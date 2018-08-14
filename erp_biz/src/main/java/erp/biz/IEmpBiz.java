package erp.biz;
import java.util.List;

import erp.entity.Emp;
import erp.entity.Menu;
import erp.entity.Tree;
/**
 * 员工业务逻辑层接口
 * @author Administrator
 *
 */
public interface IEmpBiz extends IBaseBiz<Emp>{
	//登录
	Emp login(String username,String pwd);
	//修改密码
	void updatePwd(Long uuid,String oldPwd,String newPwd);
	//重置密码
	void resetPwd(Long uuid,String newPwd);
	//获取用户角色
	List<Tree> getEmpRole(Long uuid);
	//更新用户角色
	void updateEmpRole(Long eid,String rid);
	//获取用户权限
	public Menu getMenuByEmp(Long uuid);
	//获取用户权限List
	public List<Menu> getMenuListByEmp(Long uuid);
}

