package erp.biz.impl;
import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Md5Hash;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;

import erp.biz.IEmpBiz;
import erp.biz.exception.ERPException;
import erp.dao.IEmpDao;
import erp.dao.IMenuDao;
import erp.dao.IRoleDao;
import erp.entity.Emp;
import erp.entity.Menu;
import erp.entity.Role;
import erp.entity.Tree;
/**
 * 员工业务逻辑类
 * @author Administrator
 *
 */
public class EmpBiz extends BaseBiz<Emp> implements IEmpBiz {
	
	private IEmpDao empDao;
	private IRoleDao roleDao;
	private IMenuDao menuDao;
	private Jedis jedis;
	//散列次数
	private int hashIterations =2;
	
	public void setEmpDao(IEmpDao empDao) {
		this.empDao = empDao;
		super.setBaseDao(this.empDao);
	}
	
	public void setRoleDao(IRoleDao roleDao) {
		this.roleDao = roleDao;
	}
	
	public void setMenuDao(IMenuDao menuDao) {
		this.menuDao = menuDao;
	}

	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}

	/**
	 * 登录
	 */
	public Emp login(String username,String pwd){
		pwd=md5(pwd,username);
		return empDao.login(username, pwd);
	}
	
	/**
	 * 添加用户
	 */
	public void add(Emp emp){
		//source 			想要修改的数据
		//salt				盐，搅乱加密
		//hashIterations	散列次数，加密次数		
		String pwd=md5(emp.getUsername(),emp.getUsername());
		emp.setPwd(pwd);
		empDao.add(emp);
	}
	
	/**
	 * 修改密码
	 */
	public void updatePwd(Long uuid,String oldPwd,String newPwd){
		Emp emp=empDao.find(uuid);
		oldPwd=md5(oldPwd,emp.getUsername());
		if(!oldPwd.equals(emp.getPwd())){
			throw new ERPException("原密码不正确");
		}
		empDao.updatePwd(uuid, md5(newPwd,emp.getUsername()));
	}
	
	/**
	 * 重置密码
	 */
	@RequiresPermissions("重置密码")
	public void resetPwd(Long uuid,String newPwd){
		Emp emp=empDao.find(uuid);
		newPwd=md5(newPwd,emp.getUsername());
		empDao.updatePwd(uuid, newPwd);
	}
	
	/**
	 * MD5加密算法
	 * @param resource
	 * @param salt
	 * @return
	 */
	private String md5(String resource,String salt){
		Md5Hash m=new Md5Hash(resource, salt, hashIterations);
		return m.toString();
	}
	
	/**
	 * 获取用户角色
	 */
	@Override
	@RequiresPermissions("员工角色")
	public List<Tree> getEmpRole(Long uuid) {
		Emp emp=empDao.find(uuid);
		List<Role> emprole =emp.getRoles();
		List<Role> list=roleDao.getList();
		List<Tree> tree=new ArrayList<Tree>();
		Tree t=null;
		for(Role r :list){
			t=new Tree();
			t.setId(r.getUuid().toString());
			t.setText(r.getName());
			if(emprole.contains(r)){
				t.setChecked(true);
			}
			tree.add(t);
		}
		return tree;
	}
	
	/**
	 * 修改用户角色
	 */
	@Override
	@RequiresPermissions("员工角色")
	public void updateEmpRole(Long eid, String rid) {
		Emp emp=empDao.find(eid);
		if(emp==null){
			throw new ERPException("用户不存在");
		}
		List<Role> list=new ArrayList<Role>();
		String [] ids=rid.split(",");
		for(String id:ids){
			list.add(roleDao.find(Long.parseLong(id)));
		}
		emp.setRoles(list);
		try {
			jedis.del("MenuList_"+eid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取用户权限
	 */
	public Menu getMenuByEmp(Long uuid){
		//根菜单
		Menu root=menuDao.find("0");
		//返回值
		Menu menu=cloneMenu(root);
		//用户菜单
		List<Menu> empMenu=empDao.getMenuByEmp(uuid);
		//一级容器
		Menu temp1=null;
		//二级容器
		Menu temp2=null;
		//遍历一级
		for(Menu m1:root.getMenus()){
			//如果用户有此一级
			if(empMenu.contains(m1)){
				//复制一级到temp1
				temp1=cloneMenu(m1);
				//遍历所属的二级
				for(Menu m2:m1.getMenus()){
					if(empMenu.contains(m2)){
						temp2=cloneMenu(m2);
						temp2.setMenus(null);
						temp1.getMenus().add(temp2);
					}
				}
				//添加temp1
				menu.getMenus().add(temp1);
			}
		}			
		return menu;
	}
	
	private Menu cloneMenu(Menu m){
		Menu menu=new Menu();
		menu.setIcon(m.getIcon());
		menu.setMenuid(m.getMenuid());
		menu.setMenuname(m.getMenuname());
		menu.setUrl(m.getUrl());
		menu.setMenus(new ArrayList<Menu>());
		return menu;
	}
	
	/**
	 * 获取用户权限List
	 */
	@Override
	public List<Menu> getMenuListByEmp(Long uuid) {
		String json=jedis.get("MenuList_"+uuid);
		List<Menu> list=null;
		if(json==null){
			list=empDao.getMenuByEmp(uuid);
			jedis.set("MenuList_"+uuid, JSON.toJSONString(list));
		}else{
			list=JSON.parseArray(json,Menu.class);
		}
		return list;
	}
}
