package com.example.piCloud.User;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest{
    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private UserRole userRole;

    private Integer age;

    private String location;

    private Integer phoneNo;

    private String gender;


}