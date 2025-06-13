package org.gb.stellarplayer.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void sendVerificationEmail(String to, String verificationToken) {
        try {
            log.info("Attempting to send verification email to: {}", to);
            
            String subject = "Account Verification - Stellar Player";
            String verificationUrl = "http://localhost:8080/api/auth/verify?token=" + verificationToken;
            
            String htmlContent = getVerificationEmailTemplate(verificationUrl);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates HTML content
            helper.setFrom("huyhoang6102003@gmail.com");
            
            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}. Error: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }
    
    public void sendWelcomeEmail(String to, String username) {
        try {
            String subject = "Welcome to Stellar Player!";
            
            String htmlContent = getWelcomeEmailTemplate(username);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates HTML content
            helper.setFrom("huyhoang6102003@gmail.com");
            
            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}. Error: {}", to, e.getMessage());
            // Don't throw exception here as this is not critical
        }
    }
    
    private String getVerificationEmailTemplate(String verificationLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <style>
                    * {
                      margin: 0;
                      padding: 0;
                      box-sizing: border-box;
                    }
                    body {
                      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                      background-color: #f5f5f7;
                      color: #1d1d1f;
                      line-height: 1.6;
                      -webkit-font-smoothing: antialiased;
                    }
                    .email-wrapper {
                      max-width: 600px;
                      margin: 40px auto;
                      background-color: #ffffff;
                      border-radius: 12px;
                      box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
                      overflow: hidden;
                    }
                    .header {
                      background: linear-gradient(135deg, #dcdfec 0%, #e35f82 100%);
                      padding: 40px 40px 60px;
                      text-align: center;
                      position: relative;
                    }
                    .logo {
                      width: 80px;
                      height: 80px;
                      margin: 0 auto 20px;
                      border-radius: 16px;
                      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
                    }
                    .header h1 {
                      color: #ffffff;
                      font-size: 28px;
                      font-weight: 600;
                      margin-bottom: 8px;
                      letter-spacing: -0.5px;
                    }
                    .header p {
                      color: rgba(255, 255, 255, 0.9);
                      font-size: 16px;
                      font-weight: 400;
                    }
                    .content {
                      padding: 50px 40px;
                      text-align: center;
                    }
                    .welcome-text {
                      font-size: 18px;
                      color: #1d1d1f;
                      margin-bottom: 16px;
                      font-weight: 500;
                    }
                    .description {
                      font-size: 16px;
                      color: #86868b;
                      margin-bottom: 40px;
                      line-height: 1.7;
                    }
                    .verify-button {
                      display: inline-block;
                      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                      color: #ffffff;
                      text-decoration: none;
                      padding: 16px 32px;
                      border-radius: 8px;
                      font-size: 16px;
                      font-weight: 600;
                      letter-spacing: 0.3px;
                      box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
                      transition: all 0.3s ease;
                      margin-bottom: 40px;
                    }
                    .verify-button:hover {
                      transform: translateY(-2px);
                      box-shadow: 0 8px 24px rgba(102, 126, 234, 0.5);
                    }
                    .divider {
                      height: 1px;
                      background: linear-gradient(90deg, transparent 0%, #d2d2d7 50%, transparent 100%);
                      margin: 40px 0;
                    }
                    .security-notice {
                      background-color: #f5f5f7;
                      border-radius: 8px;
                      padding: 20px;
                      margin: 30px 0;
                    }
                    .security-notice h3 {
                      font-size: 16px;
                      color: #1d1d1f;
                      margin-bottom: 8px;
                      font-weight: 600;
                    }
                    .security-notice p {
                      font-size: 14px;
                      color: #86868b;
                      line-height: 1.6;
                    }
                    .footer {
                      background-color: #f5f5f7;
                      padding: 30px 40px;
                      text-align: center;
                      border-top: 1px solid #d2d2d7;
                    }
                    .footer p {
                      font-size: 13px;
                      color: #86868b;
                      margin-bottom: 8px;
                    }
                    .footer .brand {
                      font-weight: 600;
                      color: #1d1d1f;
                    }
                    @media (max-width: 600px) {
                      .email-wrapper {
                        margin: 20px;
                        border-radius: 8px;
                      }
                      .header, .content, .footer {
                        padding: 30px 20px;
                      }
                      .header h1 {
                        font-size: 24px;
                      }
                      .logo {
                        width: 60px;
                        height: 60px;
                      }
                    }
                  </style>
                </head>
                <body>
                  <div class="email-wrapper">
                    <div class="header">
                      
                      <h1>Welcome to Stellar Player</h1>
                      <p>Your music journey begins here</p>
                    </div>
                    
                    <div class="content">
                      <p class="welcome-text">Verify your email address</p>
                      <p class="description">
                        Thank you for joining Stellar Player! To get started and ensure the security of your account, 
                        please verify your email address by clicking the button below.
                      </p>
                      
                      <a href="VERIFICATION_LINK_PLACEHOLDER" class="verify-button">Verify Email Address</a>
                      
                      <div class="divider"></div>
                      
                      <div class="security-notice">
                        <h3>🔒 Security Notice</h3>
                        <p>This verification link will expire in 24 hours for your security. If you didn't create this account, you can safely ignore this email.</p>
                      </div>
                    </div>
                    
                    <div class="footer">
                      <p>Best regards,</p>
                      <p class="brand">The Stellar Player Team</p>
                      <p style="margin-top: 20px;">© 2025 Stellar Player. All rights reserved.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.replace("VERIFICATION_LINK_PLACEHOLDER", verificationLink);
    }
    
    private String getWelcomeEmailTemplate(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <style>
                    * {
                      margin: 0;
                      padding: 0;
                      box-sizing: border-box;
                    }
                    body {
                      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                      background-color: #f5f5f7;
                      color: #1d1d1f;
                      line-height: 1.6;
                      -webkit-font-smoothing: antialiased;
                    }
                    .email-wrapper {
                      max-width: 600px;
                      margin: 40px auto;
                      background-color: #ffffff;
                      border-radius: 12px;
                      box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
                      overflow: hidden;
                    }
                    .header {
                      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                      padding: 40px 40px 60px;
                      text-align: center;
                      position: relative;
                    }
                    .logo {
                      width: 80px;
                      height: 80px;
                      margin: 0 auto 20px;
                      border-radius: 16px;
                      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
                    }
                    .header h1 {
                      color: #ffffff;
                      font-size: 28px;
                      font-weight: 600;
                      margin-bottom: 8px;
                      letter-spacing: -0.5px;
                    }
                    .header p {
                      color: rgba(255, 255, 255, 0.9);
                      font-size: 16px;
                      font-weight: 400;
                    }
                    .content {
                      padding: 50px 40px;
                      text-align: center;
                    }
                    .welcome-text {
                      font-size: 22px;
                      color: #1d1d1f;
                      margin-bottom: 16px;
                      font-weight: 600;
                    }
                    .description {
                      font-size: 16px;
                      color: #86868b;
                      margin-bottom: 40px;
                      line-height: 1.7;
                    }
                    .action-button {
                      display: inline-block;
                      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                      color: #ffffff;
                      text-decoration: none;
                      padding: 16px 32px;
                      border-radius: 8px;
                      font-size: 16px;
                      font-weight: 600;
                      letter-spacing: 0.3px;
                      box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
                      transition: all 0.3s ease;
                      margin-bottom: 40px;
                    }
                    .action-button:hover {
                      transform: translateY(-2px);
                      box-shadow: 0 8px 24px rgba(102, 126, 234, 0.5);
                    }
                    .divider {
                      height: 1px;
                      background: linear-gradient(90deg, transparent 0%, #d2d2d7 50%, transparent 100%);
                      margin: 40px 0;
                    }
                    .features-section {
                      background-color: #f5f5f7;
                      border-radius: 8px;
                      padding: 30px;
                      margin: 30px 0;
                      text-align: left;
                    }
                    .features-section h3 {
                      font-size: 18px;
                      color: #1d1d1f;
                      margin-bottom: 16px;
                      font-weight: 600;
                      text-align: center;
                    }
                    .feature-item {
                      margin-bottom: 12px;
                      font-size: 14px;
                      color: #86868b;
                      padding-left: 20px;
                      position: relative;
                    }
                    .feature-item:before {
                      content: "🎵";
                      position: absolute;
                      left: 0;
                    }
                    .footer {
                      background-color: #f5f5f7;
                      padding: 30px 40px;
                      text-align: center;
                      border-top: 1px solid #d2d2d7;
                    }
                    .footer p {
                      font-size: 13px;
                      color: #86868b;
                      margin-bottom: 8px;
                    }
                    .footer .brand {
                      font-weight: 600;
                      color: #1d1d1f;
                    }
                    @media (max-width: 600px) {
                      .email-wrapper {
                        margin: 20px;
                        border-radius: 8px;
                      }
                      .header, .content, .footer {
                        padding: 30px 20px;
                      }
                      .header h1 {
                        font-size: 24px;
                      }
                      .logo {
                        width: 60px;
                        height: 60px;
                      }
                    }
                  </style>
                </head>
                <body>
                  <div class="email-wrapper">
                    <div class="header">
                      
                      <h1>Welcome to Stellar Player</h1>
                      <p>Your account is now active!</p>
                    </div>
                    
                    <div class="content">
                      <p class="welcome-text">Hello USERNAME_PLACEHOLDER! 🎉</p>
                      <p class="description">
                        Your account has been successfully verified and activated. You can now access all the amazing features 
                        of Stellar Player and start your musical journey with us.
                      </p>
                      
                      <a href="http://localhost:3000" class="action-button">Start Listening Now</a>
                      
                      <div class="divider"></div>
                      
                      <div class="features-section">
                        <h3>🌟 What you can do now:</h3>
                        <div class="feature-item">Create and manage your personal playlists</div>
                        <div class="feature-item">Discover new music and artists</div>
                        <div class="feature-item">Stream high-quality audio content</div>
                        <div class="feature-item">Connect with other music lovers</div>
                        <div class="feature-item">Get personalized recommendations</div>
                      </div>
                    </div>
                    
                    <div class="footer">
                      <p>Thank you for choosing Stellar Player!</p>
                      <p class="brand">The Stellar Player Team</p>
                      <p style="margin-top: 20px;">© 2025 Stellar Player. All rights reserved.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.replace("USERNAME_PLACEHOLDER", username);
    }
} 