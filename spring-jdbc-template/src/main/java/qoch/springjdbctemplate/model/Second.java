package qoch.springjdbctemplate.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor @Builder
public class Second {
    private Long id;
    private Long firstId; // not uk
}
