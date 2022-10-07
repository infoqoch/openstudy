package qoch.springjdbctemplate.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor @Builder
public class First {
    public enum Status {
        NEW, DONE
    }
    private Long id;
    private Status status;
}
