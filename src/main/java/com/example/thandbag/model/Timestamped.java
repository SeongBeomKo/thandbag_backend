package com.example.thandbag.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Setter
@Getter
@MappedSuperclass // 이 클래스를 상속해서 사용하면, 사용된걸 컬럼으로 인식
@EntityListeners(AuditingEntityListener.class) //계속 주시함, 수정이 발생 시 자동 반영
/* 추상클래스(abstract)는 상속해야만 쓸 수 있음 (객체 생성 불가능) */
public abstract class Timestamped {

    @CreatedDate // 생성일자임을 나타냄
    private LocalDateTime createdAt;

    @LastModifiedDate // 마지막 수정일자임을 나타냄
    private LocalDateTime modifiedAt;
}