package utils;

import java.util.List;

import javax.mail.MessagingException;

import erp.biz.IStorealertBiz;
import erp.entity.Storealert;

public class MailJob {
	private IStorealertBiz storealertBiz;

	public void setStorealertBiz(IStorealertBiz storealertBiz) {
		this.storealertBiz = storealertBiz;
	}
	/**
	 * 任务调度
	 */
	public void sendStorealertMail(){
		List<Storealert> list=storealertBiz.getStorealert();
		if(list.size()!=0){
			try {
				storealertBiz.sendStorealertMail();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
		}
	}
	
}
