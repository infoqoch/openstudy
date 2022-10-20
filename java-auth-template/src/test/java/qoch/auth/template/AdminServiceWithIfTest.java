package qoch.auth.template;

import org.junit.jupiter.api.Test;

public class AdminServiceWithIfTest {
    @Test
    void test(){
        // 회원
        Admin supers = new Admin("super", Admin.Type.SUPER);
        Admin lee = new Admin("lee", Admin.Type.BASIC);
        Admin kim = new Admin("kim", Admin.Type.BASIC);

        // 기존에 작성된 글
        Article writtenArticle = new Article(324l, "공지사항", "내일 잠시 점검이 있을 예정입니다.", "kim");

        // 수정의 권한이 있는지 확인하고 한다.
        Admin modifier = supers;

        // if 문으로 데이터에 대한 접근 권한을 판별한다.
        // 깨지기 쉬운 코드가 된다.
        if(modifier.type == Admin.Type.SUPER){
            System.out.println("super 유저는 수정 가능합니다...");
        }else if(modifier.id.equals(writtenArticle.author)){
            System.out.println("작성자와 수정자가 일치하여 수정 가능합니다...");
        }else{
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        // 수정한다.
        writtenArticle.changeTitleAndContent("공지사항(수정)", "점검이 조기에 종료되어 오늘 자정 전에 오픈 예정입니다.");
    }

    static class Admin {
        private final String id;
        private final Type type;

        enum Type{
            SUPER, BASIC
        }

        Admin(String id, Type type) {
            this.id = id;
            this.type = type;
        }
    }

    static class Article {
        private Long no;
        private String title;
        private String content;
        private String author;

        Article(Long no, String title, String content, String author) {
            this.no = no;
            this.title = title;
            this.content = content;
            this.author = author;
        }

        public void changeTitleAndContent(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }
}
