package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {

    @Id
    @NotBlank
    @Column(name = "eventtype")
    private String eventType;
    @Column(name = "eventdescription")
    private String eventDescription;

    @Column(name = "eventowner")
    private  String eventOwner;

    @Column(name = "isactive")
    private Character isActive;

    @Column(name = "criticalevent")
    private Character critical;

    @Column(name = "isreusable")
    private Character isReusable;

    @Column(name = "ipfstage")
    private String gifStage;

    @Column(name = "updateddate")
    private LocalDateTime updatedDate;

    @Column(name = "createddate")
    private LocalDateTime createdDate;

    @Column(name = "lastupdatedby")
    private String lastUpdatedBy;





}
