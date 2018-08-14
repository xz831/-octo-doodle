package utils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class MailUtil {
	
	private JavaMailSender sender;//邮件发送类
	private String from;//发送人

	public void setSender(JavaMailSender sender) {
		this.sender = sender;
	}

	public void setFrom(String from) {
		this.from = from;
	}
	/**
	 * 
	 * @param to 		接收者
	 * @param subject	标题
	 * @param text		正文
	 * @throws MessagingException
	 */
	public void sendMail(String to,String subject,String text) throws MessagingException{
		//创建邮件
		MimeMessage mm=sender.createMimeMessage();
		//抄送一分给自己，解决163邮箱服务器报错554 DT:SPM
		mm.addRecipients(MimeMessage.RecipientType.CC, from);
		//邮件包装工具
		MimeMessageHelper mmh=new MimeMessageHelper(mm);
		//发件人
		mmh.setFrom(from);
		//收件人
		mmh.setTo(to);
		//邮件标题
		mmh.setSubject(subject);
		//邮件正文
		mmh.setText(text);
		//发送邮件
		sender.send(mm);			
	}

}
