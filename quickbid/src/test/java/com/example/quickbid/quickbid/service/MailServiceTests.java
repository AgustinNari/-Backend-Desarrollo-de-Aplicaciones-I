package com.example.quickbid.quickbid.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MailServiceTests {

    @Test
    void usesSimulatedMailServiceByDefault() {
        new ApplicationContextRunner()
                .withUserConfiguration(MailTemplates.class, SimulatedMailService.class, SmtpMailService.class)
                .withBean(JavaMailSender.class, StubJavaMailSender::new)
                .run(context -> assertInstanceOf(SimulatedMailService.class, context.getBean(MailService.class)));
    }

    @Test
    void usesSmtpMailServiceWhenEnabled() {
        new ApplicationContextRunner()
                .withUserConfiguration(MailTemplates.class, SimulatedMailService.class, SmtpMailService.class)
                .withBean(JavaMailSender.class, StubJavaMailSender::new)
                .withPropertyValues(
                        "app.mail.enabled=true",
                        "app.mail.from=no-reply@quickbid.demo"
                )
                .run(context -> assertInstanceOf(SmtpMailService.class, context.getBean(MailService.class)));
    }

    @Test
    void simulatedMailDoesNotRetainRawToken() {
        SimulatedMailService mail = new SimulatedMailService(new MailTemplates("https://frontend.quickbid.demo"));

        mail.sendToken("recuperacion", "usuario@quickbid.demo", "token-super-secreto");

        assertEquals(1, mail.deliveries().size());
        assertEquals("Recupera tu clave de QuickBid", mail.deliveries().get(0).subject());
        assertTrue(mail.deliveries().get(0).preview().contains("token-redactado"));
        assertFalse(mail.deliveries().toString().contains("token-super-secreto"));
    }

    @Test
    void simulatedMailValidatesKnownTemplateTypes() {
        SimulatedMailService mail = new SimulatedMailService(new MailTemplates("https://frontend.quickbid.demo"));

        assertThrows(IllegalArgumentException.class,
                () -> mail.sendToken("desconocido", "usuario@quickbid.demo", "token"));
        assertThrows(IllegalArgumentException.class,
                () -> mail.sendNotification("usuario@quickbid.demo", "desconocida"));
    }

    @Test
    void smtpMailBuildsRecoveryLinkAndSendsMessage() {
        StubJavaMailSender sender = new StubJavaMailSender();
        MailTemplates templates = new MailTemplates("https://frontend.quickbid.demo");
        SmtpMailService mail = new SmtpMailService(sender, templates, "no-reply@quickbid.demo");

        mail.sendToken("recuperacion", "usuario@quickbid.demo", "token con /+");

        assertNotNull(sender.lastMessage);
        assertEquals("no-reply@quickbid.demo", sender.lastMessage.getFrom());
        assertEquals("usuario@quickbid.demo", sender.lastMessage.getTo()[0]);
        assertTrue(sender.lastMessage.getText().contains(
                "https://frontend.quickbid.demo/recuperar-clave?token=token+con+%2F%2B"
        ));
        assertTrue(sender.lastMessage.getText().contains("Si no solicitaste esto, podes ignorar este mensaje."));
    }

    @Test
    void tokenTemplatesBuildAndroidDeepLinksWithoutDuplicatedSlashes() {
        MailTemplates templates = new MailTemplates("quickbid://auth/");

        MailTemplates.Message setup = templates.token("registro", "token con /+");
        MailTemplates.Message reset = templates.token("recuperacion", "token con /+");

        assertTrue(setup.body().contains("quickbid://auth/completar-registro?token=token+con+%2F%2B"));
        assertTrue(reset.body().contains("quickbid://auth/recuperar-clave?token=token+con+%2F%2B"));
    }

    @Test
    void tokenTemplatesBuildAndroidDeepLinksWhenBaseUrlHasNoTrailingSlash() {
        MailTemplates templates = new MailTemplates("quickbid://auth");

        MailTemplates.Message setup = templates.token("registro", "abc");
        MailTemplates.Message reset = templates.token("recuperacion", "abc");

        assertTrue(setup.body().contains("quickbid://auth/completar-registro?token=abc"));
        assertTrue(reset.body().contains("quickbid://auth/recuperar-clave?token=abc"));
    }

    @Test
    void smtpFailureIsExposedAsControlledMailError() {
        JavaMailSender sender = new StubJavaMailSender() {
            @Override
            public void send(SimpleMailMessage simpleMessage) throws MailSendException {
                throw new MailSendException("provider unavailable");
            }
        };
        SmtpMailService mail = new SmtpMailService(
                sender,
                new MailTemplates("https://frontend.quickbid.demo"),
                "no-reply@quickbid.demo"
        );

        assertThrows(
                MailDeliveryException.class,
                () -> mail.sendNotification("usuario@quickbid.demo", "multa_generada")
        );
    }

    @Test
    void smtpRequiresConfiguredSenderAddress() {
        assertThrows(MailDeliveryException.class,
                () -> new SmtpMailService(new StubJavaMailSender(),
                        new MailTemplates("https://frontend.quickbid.demo"), ""));
        assertThrows(MailDeliveryException.class,
                () -> new SmtpMailService(new StubJavaMailSender(),
                        new MailTemplates("https://frontend.quickbid.demo"), "no-es-email"));
    }

    @Test
    void smtpRejectsInvalidRecipientAsControlledMailError() {
        SmtpMailService mail = new SmtpMailService(
                new StubJavaMailSender(),
                new MailTemplates("https://frontend.quickbid.demo"),
                "no-reply@quickbid.demo"
        );

        assertThrows(MailDeliveryException.class,
                () -> mail.sendNotification("destinatario-invalido", "multa_generada"));
    }

    private static class StubJavaMailSender implements JavaMailSender {
        private SimpleMailMessage lastMessage;

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage(Session.getInstance(new Properties()));
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) {
            return createMimeMessage();
        }

        @Override
        public void send(MimeMessage mimeMessage) {
        }

        @Override
        public void send(MimeMessage... mimeMessages) {
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) {
            lastMessage = simpleMessage;
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            if (simpleMessages.length > 0) {
                lastMessage = simpleMessages[0];
            }
        }
    }
}
