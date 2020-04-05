package com.prokopchuk.ws.service.impl;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.prokopchuk.ws.service.AmazonSES;
import com.prokopchuk.ws.shared.dto.UserDto;
import org.springframework.stereotype.Service;

@Service
public class AmazonSESImpl implements AmazonSES {
    final String FROM = "nikolyand@gmail.com";
    final String SUBJECT = "One last step to complete your registration with PhotoApp";
    final String PASSWORD_RESET_SUBJECT = "Password reset request";

    final String HTMLBODY = "<h1>Please verify your email address</h1>"
            + "<p>Thank you for registering with your mobile app. To complete registration process and be able to log in,"
            + "click the following link:"
            + "<a href='http://ec2-35-156-134-140.eu-central-1.compute.amazonaws.com:8080/verification-service/email-verification.html?token=$tokenValue'>"
            + "Final step to complete your registration" + "</a><br/><br/>"
            + "Thank you! And we are waiting for you inside!</p>";

    final String TEXTBODY = "Please verify your email address"
            + "Thank you for registering with your mobile app. To complete registration process and be able to log in,"
            + "open the following url in your browser window: "
            + "http://ec2-35-156-134-140.eu-central-1.compute.amazonaws.com:8080/verification-service/email-verification.html?token=$tokenValue"
            + "Thank you! And we are waiting for you inside!";

    final String PASSWORD_RESET_HTMLBODY = "<h1>A request to reset your password</h1>"
            + "<p>Hi $firstName!</p>"
            + "<p>Someone has requested to reset your password with our project. If it were not you, please ignore it"
            + "otherwise please click on the link below to to set a new password"
            + "<a href='http://localhost:8080/verification-service/password-reset.html?token=$tokenValue'>"
            + "Click the link to Reset your password" + "</a><br/><br/>"
            + "Thank you!</p>";

    final String PASSWORD_RESET_TEXTBODY = "A request to reset your password"
            + "Hi $firstName!"
            + "<p>Someone has requested to reset your password with our project. If it were not you, please ignore it"
            + "otherwise please open in your browser the link below to to set a new password:"
            + "http://localhost:8080/verification-service/password-reset.html?token=$tokenValue"
            + "Thank you!";

    public void verifyEmail(UserDto userDto) {
        AmazonSimpleEmailService client = AmazonSimpleEmailServiceAsyncClientBuilder
                .standard()
                .withRegion(Regions.EU_CENTRAL_1).build();
        String htmlBodyWithToken = HTMLBODY.replace("$tokenValue", userDto.getEmailVerificationToken());
        String textBodyWithToken = HTMLBODY.replace("$tokenValue", userDto.getEmailVerificationToken());
        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(userDto.getEmail()))
                .withMessage(new Message()
                        .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(htmlBodyWithToken))
                                .withText(new Content().withCharset("UTF-8").withData(textBodyWithToken)))
                        .withSubject(new Content().withCharset("UTF-8").withData(SUBJECT)))
                .withSource(FROM);

        client.sendEmail(request);
        System.out.println("Email sent!");
    }

    public boolean sendPasswordResetRequest(String firstName, String email, String token) {
        AmazonSimpleEmailService client = AmazonSimpleEmailServiceAsyncClientBuilder
                .standard()
                .withRegion(Regions.EU_CENTRAL_1).build();
        String htmlBodyWithToken = PASSWORD_RESET_HTMLBODY.replace("$tokenValue", token)
                .replace("$firstName", firstName);

        String textBodyWithToken = PASSWORD_RESET_TEXTBODY.replace("$tokenValue", token)
                .replace("$firstName", firstName);

        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(email))
                .withMessage(new Message()
                        .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(htmlBodyWithToken))
                                .withText(new Content().withCharset("UTF-8").withData(textBodyWithToken)))
                        .withSubject(new Content().withCharset("UTF-8").withData(PASSWORD_RESET_SUBJECT)))
                .withSource(FROM);
        SendEmailResult result = client.sendEmail(request);
        return result != null && (result.getMessageId() != null && !result.getMessageId().isEmpty());
    }
}
