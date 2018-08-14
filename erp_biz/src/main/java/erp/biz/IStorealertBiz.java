package erp.biz;

import java.util.List;

import javax.mail.MessagingException;

import erp.entity.Storealert;

public interface IStorealertBiz {
	//获取库存和待发货预警
	List<Storealert> getStorealert();
	//发送库存预警邮件
	void sendStorealertMail() throws MessagingException;
}
