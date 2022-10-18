package qoch.springjdbctemplate.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor @Builder
public class Second {
    private Long id;
    private Long firstId; // not uk
}
