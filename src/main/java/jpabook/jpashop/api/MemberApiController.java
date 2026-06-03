package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

//    @RequestBody는 단순 생성 + 값 세팅이기에 객체 그래프 탐색으로 인한 무한 참조가 발생하지 않는다
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);

        CreateMemberResponse createMemberResponse = new CreateMemberResponse();
        createMemberResponse.setId(id);

        return createMemberResponse;
    }

//    Orders 데이터가 없다면 무한참조가 발생하지 않음
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {

        Member member = new Member();
        member.setName(request.name);

        long id = memberService.join(member);
        CreateMemberResponse createMemberResponse = new CreateMemberResponse();
        createMemberResponse.setId(id);
        return createMemberResponse;
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @GetMapping("/api/v2/members")
    public Result<List<MemberDto>> membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result<>(collect.size(), collect);
    }

    @Data
    public static class CreateMemberResponse {

        private Long id;
    }

    @Data
    public static class CreateMemberRequest {

        @NotEmpty
        private String name;
    }

    @Data
    public static class UpdateMemberRequest {

        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class UpdateMemberResponse {

        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class Result<T> {

        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    public static class MemberDto {

        private String name;
    }
}
