-- KEYS[1]: Java에서 전달한 전체 키 (예: runner:Pxrg...:token_rts)

-- 1. 키로 현재 값을 조회합니다.
local currentValue = redis.call('GET', KEYS[1])

-- 2. 값이 없으면(nil) 즉시 nil을 반환합니다.
-- (Java의 Mono.empty()로 처리되어 NOT_FOUND_REFRESH_TOKEN 예외 발생)
if not currentValue then
    return nil
end

-- 3. 'isValid' 키에 대해 공백이 없는 버전과 있는 버전을 모두 준비합니다.
local pattern_no_space = '"isValid":true'
local pattern_with_space = '"isValid": true'
-- 무효화 시킬 때는 공백 없는 표준 형태로 저장합니다.
local replacement = '"isValid":false'

local found_pattern = nil

-- 4. 'isValid:true' 상태인지 확인합니다.
-- 4a. 공백 없는 버전 ("isValid":true) 확인
if string.find(currentValue, pattern_no_space) then
    found_pattern = pattern_no_space
-- 4b. 공백 있는 버전 ("isValid": true) 확인
elseif string.find(currentValue, pattern_with_space) then
    found_pattern = pattern_with_space
end

-- 5. 'true' 상태의 토큰을 찾지 못했다면 (이미 'false'이거나 형식이 다름)
--    아무 작업도 하지 않고 원본 값을 반환합니다.
-- (Java의 filter(RefreshTokenMetadata::isValid)에서 이 요청을 거부할 것입니다)
if not found_pattern then
    return currentValue
end

-- 6. 'true' 토큰을 찾았으므로, 즉시 무효화 작업을 진행합니다.
local ttl = redis.call('TTL', KEYS[1])

-- 7. 찾았던 패턴(공백 있든 없든)을 표준 'false' 형태로 교체하여 새 값을 만듭니다.
local newValue = string.gsub(currentValue, found_pattern, replacement)

-- 8. 기존 TTL을 유지하면서 'newValue'("isValid":false" 포함)로 덮어씁니다.
if ttl > 0 then
    redis.call('SET', KEYS[1], newValue, 'EX', ttl)
else
    -- TTL이 없거나(-1) 만료 직전(-2)이면 그냥 SET
    redis.call('SET', KEYS[1], newValue)
end

-- 9. Java 쪽에는 무효화되기 *전*의 '원본 값' ("isValid":true" 포함)을 반환합니다.
return currentValue