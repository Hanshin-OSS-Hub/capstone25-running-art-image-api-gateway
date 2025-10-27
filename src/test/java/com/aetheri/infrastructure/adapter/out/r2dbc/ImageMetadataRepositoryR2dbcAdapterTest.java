package com.aetheri.infrastructure.adapter.out.r2dbc;

import com.aetheri.application.command.imagemetadata.ImageMetadataSaveCommand;
import com.aetheri.application.command.imagemetadata.ImageMetadataUpdateCommand;
import com.aetheri.application.port.out.r2dbc.ImageMetadataR2dbcRepository;
import com.aetheri.domain.enums.image.Proficiency;
import com.aetheri.domain.enums.image.Shape;
import com.aetheri.domain.model.ImageMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageMetadataRepositoryR2dbcAdapterTest {

    @Autowired
    ImageMetadataRepositoryR2dbcAdapter adapter;

    @Autowired
    ImageMetadataR2dbcRepository r2dbcRepository;

    private final long runnerId = 1L;
    private final String location = "한신대학교";

    @BeforeEach
    void setUp() {
        // (주의) .block()은 테스트 코드에서만 허용됩니다!
        // 매 테스트 시작 전, 'runnerId'에 해당하는 모든 데이터를 삭제합니다.
        r2dbcRepository.deleteAllByRunnerId(runnerId).block();
    }

    @Test
    void saveImageMetadata() {
        // 1. 테스트할 메서드를 호출하고 반환되는 Mono<Long>을 저장합니다.
        // adapter는 실제 저장 로직을 수행하는 컴포넌트(예: 서비스 또는 리포지토리)입니다.
        // saveImageMetadata 메서드는 이미지 메타데이터를 저장하고, 저장된 데이터의 ID(Long 타입)를 Mono로 반환합니다.
        Mono<Long> save = adapter.saveImageMetadata(
                // runnerId: 메타데이터를 연결할 엔티티의 ID (예: 사용자 ID).
                runnerId,
                // new ImageMetadataSaveCommand(...): 저장할 이미지 메타데이터를 담는 DTO(Data Transfer Object) 또는 커맨드 객체입니다.
                new ImageMetadataSaveCommand(
                        // "한신대학교": 이미지의 이름, 출처, 또는 기타 식별 가능한 문자열 정보.
                        location,
                        // Proficiency.ADVANCED: 이미지에 관련된 숙련도 또는 레벨 정보 (열거형).
                        Proficiency.ADVANCED,
                        // Shape.CIRCLE: 이미지의 형태 정보 (열거형).
                        Shape.CIRCLE
                ));

        // 2. Project Reactor의 StepVerifier를 사용하여 Mono의 비동기 결과를 검증합니다.
        StepVerifier.create(save)
                // .assertNext(savedId -> { ... }): Mono가 데이터를 성공적으로 방출(emit)하는지 확인하고,
                // 방출된 데이터(savedId, 즉 저장된 이미지 메타데이터의 ID)에 대한 추가 검증을 수행합니다.
                .assertNext(savedId -> {
                    // assertThat(savedId).isNotNull(): 저장된 ID가 null이 아닌지 확인합니다.
                    assertThat(savedId).isNotNull();
                    // assertThat(savedId).isGreaterThan(0L): 저장된 ID가 유효한 값(0보다 큰 양수)인지 확인합니다.
                    // 이는 데이터가 실제로 저장되었고 유효한 Primary Key가 할당되었음을 의미합니다.
                    assertThat(savedId).isGreaterThan(0L);
                })
                // .verifyComplete(): Mono가 오류 없이 정상적으로 완료(onComplete 시그널)되었는지 확인합니다.
                .verifyComplete();
    }


    @Test
    void findById() {
        // 1. 테스트 환경 설정 (Setup): 데이터를 저장하고 즉시 조회하는 비동기 체인 구축
        // Mono<ImageMetadata> setup: 최종적으로 조회된 ImageMetadata 객체를 담을 Mono입니다.
        Mono<ImageMetadata> setup = adapter.saveImageMetadata(
                        // runnerId: 메타데이터를 연결할 엔티티 ID (저장할 데이터의 일부)
                        runnerId,
                        // ImageMetadataSaveCommand: 저장할 메타데이터의 내용 (위치, 숙련도, 형태)
                        new ImageMetadataSaveCommand(
                                location, Proficiency.ADVANCED, Shape.CIRCLE
                        ))
                // .flatMap(savedId -> ...): 저장 작업이 완료되고 ID가 방출되면, 그 ID를 사용하여 다음 작업을 비동기적으로 연결합니다.
                // savedId: 저장 후 반환된 Primary Key
                .flatMap(savedId ->
                        // adapter.findById(savedId): 저장된 ID를 이용해 데이터베이스에서 해당 메타데이터를 조회합니다.
                        adapter.findById(savedId));

        // 2. 결과 검증 (Verification): StepVerifier를 사용하여 비동기 체인의 최종 결과를 확인합니다.
        StepVerifier.create(setup)
                // .assertNext(image -> { ... }): Mono가 성공적으로 데이터를 방출하는지 확인하고, 방출된 ImageMetadata 객체(image)를 검증합니다.
                .assertNext(image -> {
                    // assertThat(image.getRunnerId()).isEqualTo(runnerId):
                    // 조회된 이미지 객체의 runnerId가 처음에 저장할 때 사용한 runnerId와 일치하는지 확인합니다. (외래 키 검증)
                    assertThat(image.getRunnerId()).isEqualTo(runnerId);

                    // assertThat(image.getTitle()).isEqualTo(location + " " + Shape.CIRCLE):
                    // 조회된 이미지 객체의 title 필드가 저장 시 입력한 location과 Shape.CIRCLE을 조합한 예상 값과 일치하는지 확인합니다.
                    // 이는 저장 로직이 title을 예상대로 생성했는지와 조회 로직이 데이터를 정확히 가져왔는지 동시에 검증합니다.
                    assertThat(image.getTitle()).isEqualTo(location + " " + Shape.CIRCLE);
                })
                // .verifyComplete(): Mono 스트림이 성공적으로 데이터를 방출하고 오류 없이 완료되었는지 확인합니다.
                .verifyComplete();
    }

    @Test
    void findByRunnerId() {
        // 1. 테스트 환경 설정 (Setup - 데이터 저장)
        // Mono<Long> saveOperation: 저장 작업을 수행하는 Mono입니다.
        // 여기서는 하나의 ImageMetadata를 저장합니다.
        Mono<Long> saveOperation = adapter.saveImageMetadata(
                // runnerId: 조회할 기준이 될 엔티티 ID (예: 사용자 ID)
                runnerId,
                // ImageMetadataSaveCommand: 저장할 메타데이터 내용
                new ImageMetadataSaveCommand(
                        location, Proficiency.ADVANCED, Shape.CIRCLE
                ));

        // 2. 실행 (Execution - 조회 체인 구축)
        // Flux<ImageMetadata> result: 조회된 ImageMetadata 목록을 담을 Flux입니다.
        Flux<ImageMetadata> result = saveOperation
                // .thenMany(...): saveOperation(저장)이 완료된 후, 그 결과를 무시하고 다음 Flux 작업을 시작합니다.
                // 저장 작업이 성공적으로 완료되었음을 보장한 후 조회 작업을 실행하기 위함입니다.
                .thenMany(adapter.findByRunnerId(runnerId)); // 저장 시 사용한 runnerId로 모든 메타데이터를 조회합니다.

        // 3. 검증 (Verification - StepVerifier를 사용한 비동기 결과 확인)
        StepVerifier.create(result)
                // .assertNext(image -> { ... }): Flux에서 방출되는 첫 번째(이자 유일한) 데이터 항목을 검증합니다.
                .assertNext(image -> {
                    // 1. 1번에서 저장한 이미지가 와야 함 (주석에서 명시된 검증 목적)
                    // 조회된 이미지의 runnerId가 저장 시 사용한 ID와 일치하는지 확인합니다.
                    assertThat(image.getRunnerId()).isEqualTo(runnerId);
                    // 조회된 이미지의 title이 예상한 값(저장 시 입력된 값)과 일치하는지 확인합니다.
                    assertThat(image.getTitle()).isEqualTo(location + " " + Shape.CIRCLE);
                })
                // .verifyComplete(): Flux에서 더 이상 데이터가 방출되지 않고(1개만 저장했으므로) 스트림이 완료되었는지 확인합니다.
                // 2. 1개만 저장했으므로, 더 이상 값이 오지 않고 완료되어야 함 (주석에서 명시된 검증 목적)
                .verifyComplete();
    }

    @Test
    void updateImageMetadata() {
        // --- 1. Arrange (테스트 데이터 정의) ---
        String newTitle = "새로운 제목";
        String newDescription = "새로운 설명";

        // 저장할 원본 데이터
        ImageMetadataSaveCommand saveCommand = new ImageMetadataSaveCommand(
                location,
                Proficiency.ADVANCED,
                Shape.CIRCLE
        );

        // 적용할 업데이트 데이터
        ImageMetadataUpdateCommand updateCommand = new ImageMetadataUpdateCommand(
                newTitle,
                newDescription
        );

        // --- 2. Act (실행) & 3. Assert (검증) ---

        // (Arrange) 먼저 데이터를 저장합니다.
        Mono<ImageMetadata> result = adapter.saveImageMetadata(runnerId, saveCommand)
                .flatMap(savedId ->
                        // (Act) 저장된 ID를 사용해 업데이트를 실행합니다.
                        adapter.updateImageMetadata(runnerId, savedId, updateCommand)
                                // (Assert) 업데이트가 완료되면,
                                // .then()을 사용해 업데이트된 데이터를 '다시 조회'합니다.
                                .then(adapter.findById(savedId))
                );


        // StepVerifier는 'result' 체인 전체를 구독(실행)합니다.
        StepVerifier.create(result)
                .assertNext(entity -> {
                    // '다시 조회한' 엔티티가 업데이트된 값을 가졌는지 검증합니다.
                    assertThat(entity.getId()).isNotNull(); // ID가 존재하는지
                    assertThat(entity.getRunnerId()).isEqualTo(runnerId);
                    assertThat(entity.getTitle()).isEqualTo(newTitle); // '새로운 제목'인지
                    assertThat(entity.getDescription()).isEqualTo(newDescription); // '새로운 설명'인지
                })
                .verifyComplete(); // 스트림이 정상적으로 완료되었는지
    }

    @Test
    void isExistImageMetadata_WhenExists() {
        // --- 1. Arrange (데이터 준비) ---
        // 이 테스트가 확인할 데이터를 '먼저' 저장합니다.
        ImageMetadataSaveCommand saveCommand = new ImageMetadataSaveCommand(
                location,
                Proficiency.ADVANCED,
                Shape.CIRCLE
        );

        // --- 2. Act (실행) ---
        // 저장이 완료된 후(flatMap), 저장된 ID를 사용해 '존재 여부'를 확인합니다.
        Mono<Boolean> result = adapter.saveImageMetadata(runnerId, saveCommand)
                .flatMap(savedId ->
                        adapter.isExistImageMetadata(savedId)
                );

        // --- 3. Assert (검증) ---
        StepVerifier.create(result)
                .expectNext(true) // 'true'가 반환될 것을 기대 (assertNext보다 간결함)
                .verifyComplete();
    }

    @Test
    void deleteById_ShouldRemoveDataFromDB() {
        // --- 1. Arrange (데이터 준비) ---
        ImageMetadataSaveCommand saveCommand = new ImageMetadataSaveCommand(
                location, Proficiency.ADVANCED, Shape.CIRCLE
        );

        // --- 2. Act & 3. Assert (실행 및 검증) ---

        // 'result' Mono는 '저장 -> 삭제 -> 존재 여부 확인'의
        // 전체 시퀀스가 완료된 후 'false'를 반환하도록 설계합니다.
        Mono<Boolean> result = adapter.saveImageMetadata(runnerId, saveCommand)
                .flatMap(savedId ->
                        // (Act) 'delete'를 실행
                        adapter.deleteById(runnerId, savedId)
                                // (Assert) 삭제가 완료된 '후에'(.then)
                                // (Assert) 정말로 삭제되었는지 '존재 여부'를 확인
                                .then(adapter.isExistImageMetadata(savedId))
                );

        // StepVerifier는 'result' 체인(Mono<Boolean>)을 실행합니다.
        StepVerifier.create(result)
                // 'isExistImageMetadata'가 'false'를 반환했는지 검증
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void deleteByRunnerId_ShouldDeleteAll() {
        // --- 1. Arrange (데이터 2개 준비) ---
        ImageMetadataSaveCommand save1 = new ImageMetadataSaveCommand(location, Proficiency.ADVANCED, Shape.CIRCLE);
        ImageMetadataSaveCommand save2 = new ImageMetadataSaveCommand(location, Proficiency.BEGINNER, Shape.SQUARE);

        // 2개의 'save' Mono를 Flux.merge로 합치고, .then()으로 모두 완료되길 기다림
        Mono<Void> setup = Flux.merge(
                adapter.saveImageMetadata(runnerId, save1),
                adapter.saveImageMetadata(runnerId, save2)
        ).then(); // 2개 저장이 모두 완료되면 Mono<Void> 반환

        // --- 2. Act ---
        // 'setup'(2개 저장)이 완료된 후 'delete' 실행
        Mono<Long> result = setup.then(adapter.deleteByRunnerId(runnerId));

        // --- 3. Assert ---
        StepVerifier.create(result)
                .assertNext(deletedCount -> {
                    // 2개가 삭제되었는지 검증
                    assertThat(deletedCount).isEqualTo(2L);
                })
                .verifyComplete();
    }
}