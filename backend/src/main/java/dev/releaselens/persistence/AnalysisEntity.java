package dev.releaselens.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="analyses")
public class AnalysisEntity {
    @Id private String id; @Column(nullable=false) private String status; @Lob private String payload; @Column(nullable=false) private Instant createdAt=Instant.now();
    protected AnalysisEntity(){}
    public AnalysisEntity(String id,String status,String payload){this.id=id;this.status=status;this.payload=payload;}
    public String getId(){return id;} public String getStatus(){return status;} public String getPayload(){return payload;} public void setStatus(String s){status=s;} public void setPayload(String p){payload=p;}
}
