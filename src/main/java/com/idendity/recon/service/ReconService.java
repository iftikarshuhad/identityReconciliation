package com.idendity.recon.service;

import com.idendity.recon.model.Contact;
import com.idendity.recon.model.IdentityRequestDto;
import com.idendity.recon.model.IdentityResponseDto;
import com.idendity.recon.repository.ContactRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Service
@Slf4j
@AllArgsConstructor
public class ReconService {

    private final ContactRepository contactRepository;

    @SneakyThrows
    public IdentityResponseDto saveIdentityRequest(IdentityRequestDto identityRequestDto) {

        if(!StringUtils.hasLength(identityRequestDto.getEmail()) &&
                !StringUtils.hasLength(identityRequestDto.getPhoneNumber())) {
            throw new IllegalAccessException("At least one value should be available");
        }

        Contact emailContact = null;
        Contact phoneContact = null;
        if(StringUtils.hasLength(identityRequestDto.getEmail())) {
            emailContact = contactRepository.getEmailContact(identityRequestDto.getEmail());
        }
        if(StringUtils.hasLength(identityRequestDto.getPhoneNumber())) {
            phoneContact = contactRepository.getPhoneContact(identityRequestDto.getPhoneNumber());
        }

        if(!ObjectUtils.isEmpty(emailContact) &&
                !ObjectUtils.isEmpty(phoneContact) &&
                (emailContact.getId().equals(phoneContact.getId()) ||
                        emailContact.getId().equals(phoneContact.getLinkedId()) ||
                        emailContact.getLinkedId().equals(phoneContact.getId()) ||
                        emailContact.getLinkedId().equals(phoneContact.getLinkedId())))
        {
            Integer id;
            if(emailContact.getLinkPrecedence().equals("primary")) {
                id = emailContact.getId();
            }
            else {
                id = emailContact.getLinkedId();
            }
            return identityResponse(id);
        }
        else if(ObjectUtils.isEmpty(emailContact) && ObjectUtils.isEmpty(phoneContact)) {
            Contact contact = saveContact(identityRequestDto.getEmail(), identityRequestDto.getPhoneNumber(), null, "primary");
            return identityResponse(contact.getId());
        }
        else if(!ObjectUtils.isEmpty(emailContact) && !ObjectUtils.isEmpty(phoneContact)) {
            Integer id;
            if(emailContact.getLinkPrecedence().equals("primary") &&
                    phoneContact.getLinkPrecedence().equals("primary")) {
                id = emailContact.getId();
                updatePhoneContactAsSecondary(emailContact, phoneContact);
            }
            else if(emailContact.getLinkPrecedence().equals("primary") &&
                    phoneContact.getLinkPrecedence().equals("secondary")) {
                id = emailContact.getId();
                Contact primaryPhoneContact = contactRepository.getPrimaryContactByLinkedId(phoneContact.getLinkedId());
                updatePhoneContactAsSecondary(emailContact, primaryPhoneContact);
            }
            else if(emailContact.getLinkPrecedence().equals("secondary") &&
                    phoneContact.getLinkPrecedence().equals("primary")) {
                Contact primaryEmailContact = contactRepository.getPrimaryContactByLinkedId(emailContact.getLinkedId());
                id = primaryEmailContact.getId();
                updatePhoneContactAsSecondary(primaryEmailContact, phoneContact);
            }
            else {
                Contact primaryEmailContact = contactRepository.getPrimaryContactByLinkedId(emailContact.getLinkedId());
                Contact primaryPhoneContact = contactRepository.getPrimaryContactByLinkedId(phoneContact.getLinkedId());
                id = primaryEmailContact.getId();
                updatePhoneContactAsSecondary(primaryEmailContact, primaryPhoneContact);
            }
            return identityResponse(id);
        }
        else if (!ObjectUtils.isEmpty(emailContact)) {
            if(!StringUtils.hasLength(identityRequestDto.getPhoneNumber())) {
                Integer id;
                if(emailContact.getLinkPrecedence().equals("primary")) {
                    id = emailContact.getId();
                }
                else {
                    id = emailContact.getLinkedId();
                }
                return identityResponse(id);
            }
            Contact contact;
            if(emailContact.getLinkPrecedence().equals("primary")) {
                contact = saveContact(identityRequestDto.getEmail(), identityRequestDto.getPhoneNumber(), emailContact.getId(), "secondary");
            }
            else {
                contact = saveContact(identityRequestDto.getEmail(), identityRequestDto.getPhoneNumber(), emailContact.getLinkedId(), "secondary");
            }
            return identityResponse(contact.getLinkedId());
        }
        else if (!ObjectUtils.isEmpty(phoneContact)) {
            if(!StringUtils.hasLength(identityRequestDto.getEmail())) {
                Integer id;
                if(phoneContact.getLinkPrecedence().equals("primary")) {
                    id = phoneContact.getId();
                }
                else {
                    id = phoneContact.getLinkedId();
                }
                return identityResponse(id);
            }
            Contact contact;
            if(phoneContact.getLinkPrecedence().equals("primary")) {
                contact = saveContact(identityRequestDto.getEmail(), identityRequestDto.getPhoneNumber(), phoneContact.getId(), "secondary");
            }
            else {
                contact = saveContact(identityRequestDto.getEmail(), identityRequestDto.getPhoneNumber(), phoneContact.getLinkedId(), "secondary");
            }
            return identityResponse(contact.getLinkedId());
        }
        else {
            log.info("Checker");
        }
        return null;
    }

    @SneakyThrows
    private Contact saveContact(String emailId, String phoneNumber, Integer linkedId, String linkPrecedence) {
        LocalDateTime currentTime = LocalDateTime.now();
        Contact contact = new Contact();
        contact.setEmail(emailId);
        contact.setPhoneNumber(phoneNumber);
        contact.setLinkedId(linkedId);
        contact.setLinkPrecedence(linkPrecedence);
        contact.setCreatedAt(currentTime);
        contact.setUpdatedAt(currentTime);
        return contactRepository.save(contact);
    }

    @SneakyThrows
    private void updatePhoneContactAsSecondary(Contact emailContact, Contact phoneContact) {
        List<Contact> secondaryContactListByPhoneContact = contactRepository.getAllSecondaryContact(phoneContact.getId());
        phoneContact.setLinkedId(emailContact.getId());
        phoneContact.setLinkPrecedence("secondary");
        updateAllContactLinkedId(emailContact.getId(), secondaryContactListByPhoneContact, phoneContact);
    }

    @SneakyThrows
    private void updateAllContactLinkedId(Integer id , List<Contact> contactList, Contact phoneContact) {
        List<Contact> updatedList = new ArrayList<>();
        updatedList.add(phoneContact);
        if(!CollectionUtils.isEmpty(contactList)) {
            for(Contact contact: contactList) {
                contact.setLinkedId(id);
                updatedList.add(contact);
            }
        }
        contactRepository.saveAll(updatedList);
    }

    @SneakyThrows
    private IdentityResponseDto identityResponse(Integer primaryId) {
        Contact primaryContact = contactRepository.getPrimaryContactByLinkedId(primaryId);
        List<Contact> secondaryContactList = contactRepository.getAllSecondaryContact(primaryId);

        IdentityResponseDto identityResponseDto = new IdentityResponseDto();
        IdentityResponseDto.ContactResponse contactResponse = new IdentityResponseDto.ContactResponse();

        contactResponse.setPrimaryContactId(primaryId);

        List<String> emailList = new ArrayList<>();
        List<String> phoneNumberList = new ArrayList<>();
        List<Integer> secondaryIdList = new ArrayList<>();

        if(StringUtils.hasLength(primaryContact.getEmail())) {
            emailList.add(primaryContact.getEmail());
        }

        if(StringUtils.hasLength(primaryContact.getPhoneNumber())) {
            phoneNumberList.add(primaryContact.getPhoneNumber());
        }

        if(!CollectionUtils.isEmpty(secondaryContactList)) {
            for(Contact contact : secondaryContactList) {
                if(StringUtils.hasLength(contact.getEmail()) && !emailList.contains(contact.getEmail())) {
                    emailList.add(contact.getEmail());
                }
                if(StringUtils.hasLength(contact.getPhoneNumber()) && !phoneNumberList.contains(contact.getPhoneNumber())) {
                    phoneNumberList.add(contact.getPhoneNumber());
                }
                secondaryIdList.add(contact.getId());
            }
        }

        contactResponse.setEmails(emailList);
        contactResponse.setPhoneNumbers(phoneNumberList);
        contactResponse.setSecondaryContactIds(secondaryIdList);

        identityResponseDto.setContact(contactResponse);

        return identityResponseDto;
    }
}
