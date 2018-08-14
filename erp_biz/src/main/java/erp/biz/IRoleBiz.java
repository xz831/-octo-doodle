package erp.biz;
import java.util.List;
import erp.entity.Role;
import erp.entity.Tree;
/**
 * 角色业务逻辑层接口
 * @author Administrator
 *
 */
public interface IRoleBiz extends IBaseBiz<Role>{
	//获取角色权限列表
	List<Tree> getRoleMenu(Long id);
	//更新角色权限
	void updateRoleMenu(Long rid,String ids);
}

