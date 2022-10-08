package qoch.springjdbctemplate.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import qoch.springjdbctemplate.model.First;
import qoch.springjdbctemplate.model.Second;

@Data
@AllArgsConstructor
public class MyRequest {
    private Long firstId;

    public Second toSecond() {
        return Second.builder().firstId(firstId).build();
    }

    public First toFirst(First.Status status) {
        return First.builder().id(firstId).status(status).build();
    }
}
