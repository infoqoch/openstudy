package qoch.auth.template;

import org.junit.jupiter.api.Test;


public class AdminServiceV1Test {

    @Test
    void test(){
        // 어드민이 super와 그외 어드민 두 개가 있다.
        Admin supers = new Admin("super", Admin.Type.SUPER);
        Admin lee = new Admin("lee", Admin.Type.BASIC);
        Admin kim = new Admin("kim", Admin.Type.BASIC);

        // 작성된 글이 있고 기본 어드민의 kim이 작성했다.
        Article article = new Article(324l, "공지사항", "내일 잠시 점검이 있을 예정입니다.", "kim");

        // 앞서의 글을 변경하고 싶다. 변경하고자 하는 내용은 아래와 같다.
        ArticleModifyRequest request = new ArticleModifyRequest(324l, "공지사항[수정]", "점검이 이번 주까지 연장될 예정입니다.");

        // 현재 세션에 어떤 어드민이 있는지 모른다.
        Admin admin = supers;

        // 해당 어드민이 super이면 삭제할 수 있고 그렇지 않으면 아이디가 일치해야 가능하다.
        modifyArticle(request, admin);


    }

    private void modifyArticle(ArticleModifyRequest request, Admin admin) {
//        Article article = articleRepository.findById(request.no);
//        if(admin.type == SUPER) {
//            article.modify(request, admin);
//        }else if(admin.id == article.reg) {
//            article.modify(request, admin);
//        }else{
//            throw new IllegalArgumentException("정상 회원이 아닙니다.");
//        }
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
        private final Long no;
        private final String title;
        private final String content;
        private final String reg;

        Article(Long no, String title, String content, String reg) {
            this.no = no;
            this.title = title;
            this.content = content;
            this.reg = reg;
        }
    }

    static class ArticleModifyRequest {
        private final Long no;
        private final String title;
        private final String content;

        ArticleModifyRequest(Long no, String title, String content) {
            this.no = no;
            this.title = title;
            this.content = content;
        }
    }
}
