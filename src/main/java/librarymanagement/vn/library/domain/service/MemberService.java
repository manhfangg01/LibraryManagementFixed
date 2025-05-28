package librarymanagement.vn.library.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import librarymanagement.vn.library.domain.dto.MemberFilterCriteriaDTO;
import librarymanagement.vn.library.domain.model.Member;
import librarymanagement.vn.library.domain.repository.MemberRepository;
import librarymanagement.vn.library.domain.service.specification.MemberSpecs;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberSpecs memberSpecs;

    public MemberService(MemberRepository memberRepository, MemberSpecs memberSpecs) {
        this.memberRepository = memberRepository;
        this.memberSpecs = memberSpecs;
    }

    public List<Member> fetchAllMember() {
        return this.memberRepository.findAll();
    }

    public Optional<Member> fetchMemberById(long id) {
        return this.memberRepository.findById(id);
    }

    public void create(Member member) {
        this.memberRepository.save(member);
    }

    public void update(Member member) {
        Member realMember = this.memberRepository.findById(member.getId()).get();
        realMember.setAddress(member.getAddress());
        realMember.setBorrows(member.getBorrows());
        realMember.setEmail(member.getEmail());
        realMember.setMembershipType(member.getMembershipType());
        realMember.setMembershipDate(member.getMembershipDate());
        realMember.setName(member.getName());
        realMember.setPhone(member.getPhone());
        realMember.setStatus(member.isStatus());
        realMember.setImageUrl(member.getImageUrl());
        this.memberRepository.save(realMember);
    }

    public void delete(Member member) {
        this.memberRepository.delete(member);
    }

    public Page<Member> fetchAllMembersWithPagination(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    public Page<Member> fetchAllMembersWithPaginationAndSpecification(MemberFilterCriteriaDTO memberFilterCriteriaDTO,
            Pageable pageable) {
        Specification<Member> spec = Specification.where(null);
        Specification<Member> spec1 = this.memberSpecs.hasEmail(memberFilterCriteriaDTO.getEmail());
        Specification<Member> spec2 = this.memberSpecs.hasMemberType(memberFilterCriteriaDTO.getMembershipType());
        Specification<Member> spec3 = this.memberSpecs.hasPhone(memberFilterCriteriaDTO.getPhone());
        Specification<Member> spec4 = this.memberSpecs.hasStatus(memberFilterCriteriaDTO.getStatus());
        Specification<Member> spec5 = this.memberSpecs.nameLike(memberFilterCriteriaDTO.getName());
        spec = spec.and(spec1).and(spec2).and(spec3).and(spec4).and(spec5);
        return this.memberRepository.findAll(spec, pageable);
    }

}
