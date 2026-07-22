package com.example.review.service;

import com.example.review.entity.Branch;
import com.example.review.repository.BranchRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(
            BranchRepository branchRepository
    ) {
        this.branchRepository = branchRepository;
    }

    // ==================================================
    // LẤY TẤT CẢ CHI NHÁNH
    // ==================================================

    @Transactional(readOnly = true)
    public List<Branch> getAllBranches() {
        return branchRepository.findAllByOrderByIdDesc();
    }

    // ==================================================
    // LẤY CHI NHÁNH THEO ID
    // ==================================================

    @Transactional(readOnly = true)
    public Branch getById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "ID chi nhánh không hợp lệ"
            );
        }

        return branchRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy chi nhánh ID: " + id
                        )
                );
    }

    // ==================================================
    // TẠO CHI NHÁNH
    // ==================================================

    @Transactional
    public Branch createBranch(
            String name,
            String address
    ) {

        String normalizedName = normalizeRequiredName(name);
        String normalizedAddress = normalizeOptionalText(address);

        if (branchRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException(
                    "Tên chi nhánh đã tồn tại"
            );
        }

        String code = generateNextCode();

        if (branchRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalStateException(
                    "Mã chi nhánh đã tồn tại. Vui lòng thử lại."
            );
        }

        Branch branch = new Branch();

        branch.setCode(code);
        branch.setName(normalizedName);
        branch.setAddress(normalizedAddress);
        branch.setActive(true);

        return branchRepository.save(branch);
    }

    // ==================================================
    // CẬP NHẬT CHI NHÁNH
    // ==================================================

    @Transactional
    public Branch updateBranch(
            Long id,
            String name,
            String address
    ) {

        Branch branch = getById(id);

        String normalizedName = normalizeRequiredName(name);
        String normalizedAddress = normalizeOptionalText(address);

        boolean nameChanged =
                !branch.getName().equalsIgnoreCase(normalizedName);

        if (
                nameChanged &&
                branchRepository.existsByNameIgnoreCase(normalizedName)
        ) {
            throw new IllegalArgumentException(
                    "Tên chi nhánh đã tồn tại"
            );
        }

        branch.setName(normalizedName);
        branch.setAddress(normalizedAddress);

        return branchRepository.save(branch);
    }

    // ==================================================
    // KHÓA / MỞ KHÓA CHI NHÁNH
    // ==================================================

    @Transactional
    public Branch toggleStatus(Long id) {

        Branch branch = getById(id);

        boolean currentStatus =
                Boolean.TRUE.equals(branch.getActive());

        branch.setActive(!currentStatus);

        return branchRepository.save(branch);
    }

    // ==================================================
    // SINH MÃ CHI NHÁNH
    // CN001, CN002, CN003...
    // ==================================================

    private String generateNextCode() {

        List<Branch> branches =
                branchRepository.findAll();

        int maxNumber = 0;

        for (Branch branch : branches) {

            String code = branch.getCode();

            if (
                    code == null ||
                    !code.toUpperCase().startsWith("CN") ||
                    code.length() <= 2
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
                // Bỏ qua mã không đúng định dạng CNxxx
            }
        }

        return String.format(
                "CN%03d",
                maxNumber + 1
        );
    }

    // ==================================================
    // CHUẨN HÓA TÊN
    // ==================================================

    private String normalizeRequiredName(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên chi nhánh không được để trống"
            );
        }

        String normalizedName =
                name.trim().replaceAll("\\s+", " ");

        if (normalizedName.length() > 150) {
            throw new IllegalArgumentException(
                    "Tên chi nhánh tối đa 150 ký tự"
            );
        }

        return normalizedName;
    }

    // ==================================================
    // CHUẨN HÓA NỘI DUNG KHÔNG BẮT BUỘC
    // ==================================================

    private String normalizeOptionalText(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalizedValue =
                value.trim().replaceAll("\\s+", " ");

        if (normalizedValue.length() > 255) {
            throw new IllegalArgumentException(
                    "Địa chỉ tối đa 255 ký tự"
            );
        }

        return normalizedValue;
    }
}