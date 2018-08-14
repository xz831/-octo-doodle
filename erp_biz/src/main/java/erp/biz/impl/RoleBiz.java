package erp.biz.impl;
import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;

import erp.biz.IRoleBiz;
import erp.dao.IMenuDao;
import erp.dao.IRoleDao;
import erp.entity.Emp;
import erp.entity.Menu;
import erp.entity.Role;
import erp.entity.Tree;
/**
 * 角色业务逻辑类
 * @author Administrator
 *
 */
public class RoleBiz extends BaseBiz<Role> implements IRoleBiz {

	private IRoleDao roleDao;
	private IMenuDao menuDao;
	private Jedis jedis;
	

	public void setMenuDao(IMenuDao menuDao) {
		this.menuDao = menuDao;
	}
	public void setRoleDao(IRoleDao roleDao) {
		this.roleDao = roleDao;
		super.setBaseDao(this.roleDao);
	}
	
	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}
	/**
	 * 获取角色菜单
	 */
	@Override
	public List<Tree> getRoleMenu(Long id) {
		List<Tree> list=new ArrayList<Tree>();
		Role role =	roleDao.find(id);
		List<Menu> roleMenu=role.getMenus();
		Menu m=menuDao.find("0");
		Tree t1=null;
		Tree t2=null;
		for(Menu m1:m.getMenus()){
			t1=new Tree();
			t1.setText(m1.getMenuname());
			t1.setId(m1.getMenuid());
			for(Menu m2: m1.getMenus()){
				t2=new Tree();
				t2.setId(m2.getMenuid());
				t2.setText(m2.getMenuname());
				if(roleMenu.contains(m2)){
					t2.setChecked(true);
				}
				t1.getChildren().add(t2);
			}
			list.add(t1);
		}
		return list;
	}
	
	/**
	 * 修改角色权限
	 */
	public void updateRoleMenu(Long rid,String ids){
		Role role=roleDao.find(rid);
		List<Menu> menus=new ArrayList<Menu>();
		String[] id=ids.split(",");
		for(String s:id){
			menus.add(menuDao.find(s));
		}
		role.setMenus(menus);
		List<Emp> emps=role.getEmps();
		try {
			for(Emp emp:emps){
				jedis.del("MenuList_"+emp.getUuid());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
