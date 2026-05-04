package com.example.rbpo2.license.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.rbpo2.license.dto.ActivateLicenseRequest;
import com.example.rbpo2.license.dto.CheckLicenseRequest;
import com.example.rbpo2.license.dto.CreateLicenseRequest;
import com.example.rbpo2.license.dto.LicenseHistoryResponse;
import com.example.rbpo2.license.dto.LicenseResponse;
import com.example.rbpo2.license.dto.RenewLicenseRequest;
import com.example.rbpo2.license.dto.Ticket;
import com.example.rbpo2.license.dto.TicketResponse;
import com.example.rbpo2.license.model.DeviceLicense;
import com.example.rbpo2.license.model.License;
import com.example.rbpo2.license.model.LicenseDevice;
import com.example.rbpo2.license.model.LicenseHistory;
import com.example.rbpo2.license.model.LicenseProduct;
import com.example.rbpo2.license.model.LicenseType;
import com.example.rbpo2.license.repository.DeviceLicenseRepository;
import com.example.rbpo2.license.repository.LicenseDeviceRepository;
import com.example.rbpo2.license.repository.LicenseHistoryRepository;
import com.example.rbpo2.license.repository.LicenseRepository;
import com.example.rbpo2.model.AppUser;

@Service
public class LicenseService {

    private final LicenseProductService licenseProductService;
    private final LicenseTypeService licenseTypeService;
    private final LicenseApplicationUserService licenseApplicationUserService;
    private final LicenseRepository licenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;
    private final LicenseDeviceRepository licenseDeviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final TicketSignatureService ticketSignatureService;
    private final CodeGenerator codeGenerator;

    public LicenseService(
            LicenseProductService licenseProductService,
            LicenseTypeService licenseTypeService,
            LicenseApplicationUserService licenseApplicationUserService,
            LicenseRepository licenseRepository,
            LicenseHistoryRepository licenseHistoryRepository,
            LicenseDeviceRepository licenseDeviceRepository,
            DeviceLicenseRepository deviceLicenseRepository,
            TicketSignatureService ticketSignatureService,
            CodeGenerator codeGenerator
    ) {
        this.licenseProductService = licenseProductService;
        this.licenseTypeService = licenseTypeService;
        this.licenseApplicationUserService = licenseApplicationUserService;
        this.licenseRepository = licenseRepository;
        this.licenseHistoryRepository = licenseHistoryRepository;
        this.licenseDeviceRepository = licenseDeviceRepository;
        this.deviceLicenseRepository = deviceLicenseRepository;
        this.ticketSignatureService = ticketSignatureService;
        this.codeGenerator = codeGenerator;
    }

    @Transactional
    public LicenseResponse createLicense(CreateLicenseRequest request, Long adminId) {
        if (request.getProductId() == null || request.getTypeId() == null || request.getOwnerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId, typeId, ownerId are required");
        }
        if (request.getDeviceCount() != null && request.getDeviceCount() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deviceCount must be >= 0");
        }

        LicenseProduct product = licenseProductService.getProductOrFail(request.getProductId());
        LicenseType type = licenseTypeService.getTypeOrFail(request.getTypeId());
        AppUser owner = licenseApplicationUserService.getActiveUserOrFail(request.getOwnerId());
        AppUser admin = licenseApplicationUserService.getActiveUserOrFail(adminId);

        License license = createNewLicense(request, product, type, owner);
        License savedLicense = licenseRepository.save(license);

        LicenseHistory history = new LicenseHistory();
        history.setLicense(savedLicense);
        history.setUser(admin);
        history.setStatus("CREATED");
        history.setDescription("License created");
        licenseHistoryRepository.save(history);

        return toResponse(savedLicense);
    }

    @Transactional(readOnly = true)
    public List<LicenseResponse> getAllLicenses(Long adminId) {
        AppUser admin = licenseApplicationUserService.getActiveUserOrFail(adminId);
        if (!"ROLE_ADMIN".equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }

        return licenseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LicenseResponse getLicenseByCode(String code, Long adminId) {
        AppUser admin = licenseApplicationUserService.getActiveUserOrFail(adminId);
        if (!"ROLE_ADMIN".equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }

        License license = licenseRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));
        return toResponse(license);
    }

    @Transactional(readOnly = true)
    public List<LicenseHistoryResponse> getLicenseHistory(UUID licenseId, Long userId) {
        AppUser currentUser = licenseApplicationUserService.getActiveUserOrFail(userId);

        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));

        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
        boolean isOwner = license.getOwner() != null && license.getOwner().getId().equals(currentUser.getId());
        boolean isCurrentUser = license.getUser() != null && license.getUser().getId().equals(currentUser.getId());
        if (!isAdmin && !isOwner && !isCurrentUser) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }

        return licenseHistoryRepository.findByLicense_IdOrderByChangeDateDescIdDesc(licenseId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional
    public TicketResponse activateLicense(ActivateLicenseRequest request, Long userId) {
        if (request.getActivationKey() == null || request.getActivationKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "activationKey is required");
        }
        if (request.getDeviceMac() == null || request.getDeviceMac().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deviceMac is required");
        }

        AppUser currentUser = licenseApplicationUserService.getActiveUserOrFail(userId);

        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));

        if (license.getUser() != null && !license.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "license owned by another user");
        }

        LicenseDevice device = licenseDeviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseGet(() -> {
                    LicenseDevice newDevice = new LicenseDevice();
                    newDevice.setMacAddress(request.getDeviceMac());
                    newDevice.setName(request.getDeviceName() != null ? request.getDeviceName() : request.getDeviceMac());
                    newDevice.setUser(currentUser);
                    return licenseDeviceRepository.save(newDevice);
                });

        if (!device.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "device owned by another user");
        }

        boolean firstActivation = license.getUser() == null;

        if (firstActivation) {
            license.setUser(currentUser);
            license.setFirstActivationDate(LocalDate.now());
            int days = license.getType().getDefaultDurationInDays();
            license.setEndingDate(license.getFirstActivationDate().plusDays(days));
            licenseRepository.save(license);

            DeviceLicense dl = new DeviceLicense();
            dl.setLicense(license);
            dl.setDevice(device);
            deviceLicenseRepository.save(dl);

            LicenseHistory history = new LicenseHistory();
            history.setLicense(license);
            history.setUser(currentUser);
            history.setStatus("ACTIVATED");
            history.setDescription("First activation");
            licenseHistoryRepository.save(history);

            Ticket ticket = buildTicket(license, device);
            String signature = ticketSignatureService.signTicket(ticket);
            return new TicketResponse(ticket, signature);
        }

        boolean exists = deviceLicenseRepository.existsByLicense_IdAndDevice_Id(license.getId(), device.getId());
        if (!exists) {
            int limit = license.getDeviceCount();
            if (limit > 0) {
                long current = deviceLicenseRepository.countByLicense_Id(license.getId());
                if (current >= limit) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "device limit reached");
                }
            }

            DeviceLicense dl = new DeviceLicense();
            dl.setLicense(license);
            dl.setDevice(device);
            deviceLicenseRepository.save(dl);

            LicenseHistory history = new LicenseHistory();
            history.setLicense(license);
            history.setUser(currentUser);
            history.setStatus("ACTIVATED");
            history.setDescription("Device activation");
            licenseHistoryRepository.save(history);
        }

        Ticket ticket = buildTicket(license, device);
        String signature = ticketSignatureService.signTicket(ticket);
        return new TicketResponse(ticket, signature);
    }

    @Transactional(readOnly = true)
    public TicketResponse checkLicense(CheckLicenseRequest request, Long userId) {
        if (request.getDeviceMac() == null || request.getDeviceMac().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deviceMac is required");
        }
        if (request.getProductId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId is required");
        }

        AppUser currentUser = licenseApplicationUserService.getActiveUserOrFail(userId);
        LocalDate today = LocalDate.now();

        LicenseDevice device = licenseDeviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "device not found"));

        License license = licenseRepository.findActiveByDeviceUserAndProduct(
                        device.getId(),
                        currentUser.getId(),
                        request.getProductId(),
                        today)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));

        Ticket ticket = buildTicket(license, device);
        String signature = ticketSignatureService.signTicket(ticket);
        return new TicketResponse(ticket, signature);
    }

    @Transactional
    public TicketResponse renewLicense(RenewLicenseRequest request, Long userId) {
        if (request.getActivationKey() == null || request.getActivationKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "activationKey is required");
        }

        AppUser currentUser = licenseApplicationUserService.getActiveUserOrFail(userId);
        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));

        if (license.isBlocked()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "license is blocked");
        }

        LocalDate today = LocalDate.now();
        LocalDate endDate = license.getEndingDate();
        boolean inactive = license.getUser() == null || endDate == null;
        boolean expiringSoon = endDate != null && !endDate.isAfter(today.plusDays(7));
        if (!inactive && !expiringSoon) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "license renewal is not allowed (expires > 7 days from now)");
        }

        LocalDate baseDate = (endDate != null && endDate.isAfter(today)) ? endDate : today;
        int durationDays = license.getType().getDefaultDurationInDays();
        license.setEndingDate(baseDate.plusDays(durationDays));
        licenseRepository.save(license);

        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(currentUser);
        history.setStatus("RENEWED");
        history.setDescription("License renewed until " + license.getEndingDate());
        licenseHistoryRepository.save(history);

        Ticket ticket = buildTicket(license, null);
        String signature = ticketSignatureService.signTicket(ticket);
        return new TicketResponse(ticket, signature);
    }

    private License createNewLicense(
            CreateLicenseRequest request,
            LicenseProduct product,
            LicenseType type,
            AppUser owner
    ) {
        License license = new License();
        license.setCode(generateUniqueCode());
        license.setProduct(product);
        license.setType(type);
        license.setOwner(owner);
        license.setUser(null);
        license.setFirstActivationDate(null);
        license.setEndingDate(null);
        license.setBlocked(false);
        license.setDeviceCount(request.getDeviceCount() != null ? request.getDeviceCount() : 0);
        license.setDescription(request.getDescription());
        return license;
    }

    private String generateUniqueCode() {
        for (int i = 0; i < 20; i++) {
            String code = codeGenerator.generateCode();
            if (licenseRepository.findByCode(code).isEmpty()) {
                return code;
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to generate code");
    }

    private LicenseResponse toResponse(License license) {
        LicenseResponse resp = new LicenseResponse();
        resp.setId(license.getId());
        resp.setCode(license.getCode());
        resp.setProductId(license.getProduct().getId());
        resp.setTypeId(license.getType().getId());
        resp.setOwnerId(license.getOwner().getId());
        resp.setUserId(license.getUser() != null ? license.getUser().getId() : null);
        resp.setFirstActivationDate(license.getFirstActivationDate());
        resp.setEndingDate(license.getEndingDate());
        resp.setBlocked(license.isBlocked());
        resp.setDeviceCount(license.getDeviceCount());
        resp.setDescription(license.getDescription());
        return resp;
    }

    private LicenseHistoryResponse toHistoryResponse(LicenseHistory history) {
        return new LicenseHistoryResponse(
                history.getId(),
                history.getLicense() != null ? history.getLicense().getId() : null,
                history.getUser() != null ? history.getUser().getId() : null,
                history.getStatus(),
                history.getChangeDate(),
                history.getDescription());
    }

    private Ticket buildTicket(License license, LicenseDevice device) {
        Ticket ticket = new Ticket();
        ticket.setServerTime(LocalDateTime.now());
        ticket.setTicketLifetimeSeconds(3600);
        ticket.setFirstActivationDate(license.getFirstActivationDate());
        ticket.setExpirationDate(license.getEndingDate());
        ticket.setUserId(license.getUser() != null ? license.getUser().getId() : null);
        ticket.setDeviceId(device != null ? device.getId() : null);
        ticket.setBlocked(license.isBlocked());
        return ticket;
    }
}
