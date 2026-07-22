package com.example.review.service;

import com.example.review.entity.Branch;
import com.example.review.repository.BranchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public List<Branch> getAllBranches() {
        return branchRepository.findAllByOrderByIdDesc();
    }

    public Branch getById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy chi nhánh ID: " + id
                        )
                );
    }

    @Transactional
    public Branch createBranch(
            String name,
            String address
    ) {

        if (name == null || name.isBlank()) {
            throw new RuntimeException(
                    "Tên chi nhánh không được để trống"
            );
        }

        String normalizedName = name.trim();

        if (branchRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new RuntimeException(
                    "Tên chi nhánh đã tồn tại"
            );
        }

        Branch branch = new Branch();

        branch.setCode(generateNextCode());
        branch.setName(normalizedName);
        branch.setAddress(
                address == null ? null : address.trim()
        );
        branch.setActive(true);

        return branchRepository.save(branch);
    }

    @Transactional
    public Branch updateBranch(
            Long id,
            String name,
            String address
    ) {

        Branch branch = getById(id);

        if (name == null || name.isBlank()) {
            throw new RuntimeException(
                    "Tên chi nhánh không được để trống"
            );
        }

        branch.setName(name.trim());
        branch.setAddress(
                address == null ? null : address.trim()
        );

        return branchRepository.save(branch);
    }

    @Transactional
    public Branch toggleStatus(Long id) {

        Branch branch = getById(id);

        branch.setActive(!Boolean.TRUE.equals(branch.getActive()));

        return branchRepository.save(branch);
    }

    private String generateNextCode() {

        List<Branch> branches = branchRepository.findAll();

        int maxNumber = 0;

        for (Branch branch : branches) {

            String code = branch.getCode();

            if (
                code == null ||
                !code.toUpperCase().startsWith("CN")
            ) {
                continue;
            }

            try {
                int number = Integer.parseInt(
                        code.substring(2)
                );

                if (number > maxNumber) {
                    maxNumber = number;
                }

            } catch (NumberFormatException ignored) {
            }
        }

        return String.format("CN%03d", maxNumber + 1);
    }
}