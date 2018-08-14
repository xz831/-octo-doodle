package erp.biz.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import utils.MailUtil;
import erp.biz.IStorealertBiz;
import erp.biz.exception.ERPException;
import erp.dao.IStorealertDao;
import erp.entity.Storealert;

public class StorealertBiz implements IStorealertBiz {
	private IStorealertDao storealertDao;
	private MailUtil mailUtil;
	private String to;
	private String subject;
	private String text;
	
	public void setStorealertDao(IStorealertDao storealertDao) {
		this.storealertDao = storealertDao;
	}
	
	public void setMailUtil(MailUtil mailUtil) {
		this.mailUtil = mailUtil;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * 获取库存和待发货预警
	 */
	@Override
	public List<Storealert> getStorealert() {
		return storealertDao.getStorealert();
	}
	/**
	 * 发送库存预警邮件
	 * @throws MessagingException 
	 */
	@Override
	public void sendStorealertMail() throws MessagingException {
		List<Storealert> list=storealertDao.getStorealert();
		if(list.size()==0){
			throw new ERPException("没有需要预警的商品");
		}
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		mailUtil.sendMail(to, subject.replace("[time]", sdf.format(new Date())), text.replace("[count]", String.valueOf(list.size())));
	}

}
