package oo.getter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NoGetterTest1 {
    @Test
    void test() {
        // given
        School school = new School("서울초", "서울 종로구");
        Student student = new Student("kim", 12, school);

        // impl 1
        StudentPrint satPrint = student.satPrint();
        assertThat(satPrint.printing()).isEqualTo("서울 종로구 소재의 서울초를 재학 중인 12세, kim은(는) 올해 수능에 응시합니다.");

        // impl 2
        StudentPrint collagePrint = student.collegeApplicationPrint();
        assertThat(collagePrint.printing()).isEqualTo("서울 종로구 소재의 서울초를 재학 중인 12세, kim은(는) 올해 귀 대학에 응시합니다.");
    }

    public interface StudentPrint {
        String printing();
    }

    @AllArgsConstructor
    static class CollegeApplicationPrint implements StudentPrint{
        private final String data;

        @Override
        public String printing() {
            return data;
        }
    }

    @AllArgsConstructor
    static class SATRegisterPrint implements StudentPrint{
        private final String data;

        @Override
        public String printing() {
            return data;
        }
    }

    @Getter
    @AllArgsConstructor
    static class Student{
        private final String name;
        private final int age;
        private final School school;

        public SATRegisterPrint satPrint() {
            return new SATRegisterPrint(school.getAddress()+" 소재의 "+school.getName()+"를 재학 중인 "+age+"세, "+name+"은(는) 올해 수능에 응시합니다.");
        }

        public CollegeApplicationPrint collegeApplicationPrint() {
            return new CollegeApplicationPrint(school.getAddress()+" 소재의 "+school.getName()+"를 재학 중인 "+age+"세, "+name+"은(는) 올해 귀 대학에 응시합니다.");
        }
    }

    @Getter
    @AllArgsConstructor
    static class School{
        private final String name;
        private final String address;
    }
}