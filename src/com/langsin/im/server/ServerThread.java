package com.langsin.im.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;

import com.langsin.im.model.TeamInfo;
import com.langsin.im.model.ToolsCreateMsg;
import com.langsin.im.model.ToolsParseMsg;
import com.langsin.im.model.UserDao;
import com.langsin.im.model.UserInfo;
import com.langsin.im.msg.IMsgConstance;
import com.langsin.im.msg.MsgHead;
import com.langsin.im.msg.MsgLogin;
import com.langsin.im.msg.MsgLoginResp;
import com.langsin.im.msg.MsgReg;
import com.langsin.im.msg.MsgRegResp;
import com.langsin.im.msg.MsgTeamList;
import com.langsin.im.utils.LogTools;

/**
 * ��ʱͨ��ϵͳ: �����ͻ��������������߳��� 1.��½/ע������,2.����,������Ϣ������װ
 */
public class ServerThread extends Thread {

	private java.net.Socket client;// �߳��д����Ŀͻ�����
	private java.io.DataOutputStream dous;// ���������
	private java.io.DataInputStream dins;// ����������
	private UserInfo owerUser; // ����̴߳�������������û�����
	private boolean loginOK = false;// �Ƿ񼺵�½�ɹ��ı�־

	// ����ʱ���봫��һ��Socket���󣬼���������ͻ������������
	public ServerThread(java.net.Socket client) {
		this.client = client;
	}

	/**
	 * ����ɹ���,������Ҫ��ȡ��һ����Ϣ, �����ǵ�½��Ϣ,Ҳ������ע������
	 * @return:�Ƿ��ȡ�ɹ�
	 */
	private boolean readFirstMsg() {
		try {
			// ��װΪ�ɶ�дԭʼ�������͵����������
			this.dous = new DataOutputStream(client.getOutputStream());
			this.dins = new DataInputStream(client.getInputStream());
		
			MsgHead msg = reciveData(); // ��ȡ��һ����Ϣ:
			if (msg.getType() == IMsgConstance.command_login) {// ����ǵ�½����
				MsgLogin ml = (MsgLogin) msg;
				return checkLogin(ml);
			}
			if (msg.getType() == IMsgConstance.command_reg) {// �����ע������
				MsgReg mr = (MsgReg) msg;
				// ����ע��,����yk��,�ظ�Ӧ����Ϣ
				int ykNum = UserDao.regUser(mr.getPwd(), mr.getNikeName());
				MsgRegResp mrs = new MsgRegResp();// ����һ��Ӧ����Ϣ����
				mrs.setTotalLen(4 + 1 + 4 + 4 + 1);
				mrs.setType(IMsgConstance.command_reg_resp);
				mrs.setDest(ykNum);
				mrs.setSrc(IMsgConstance.Server_yk_NUMBER);
				mrs.setState((byte) 0);
				this.sendMsg2Me(mrs);// ����ע��Ӧ����Ϣ
			}
		} catch (Exception ef) {
			LogTools.ERROR(this.getClass(), "readFirstMsgʧ��:" + ef);
		}
		return false;
	}

	// ȡ������̶߳���������û�����
	public UserInfo getOwerUser() {
		return this.owerUser;
	}

	/** ����½�Ƿ�ɹ� msg:���յ��ĵ�½������Ϣ���� */
	private boolean checkLogin(MsgLogin msg) {
		// �����ݿ�ģ����֤�Ƿ��½�ɳɹ�
		owerUser = UserDao.checkLogin(msg.getSrc(), msg.getPwd());
		if (owerUser != null) {
			// ��½�ɹ�1.��Ӧ�����2.����������̶߳���ӵ�������
			MsgLoginResp mlr = new MsgLoginResp();
			mlr.setTotalLen(4 + 1 + 4 + 4 + 1);
			mlr.setType(IMsgConstance.command_login_resp);
			mlr.setSrc(IMsgConstance.Server_yk_NUMBER);
			mlr.setDest(owerUser.getykNum());// �����ߺ���
			mlr.setState((byte) 0);
			this.sendMsg2Me(mlr);// ����Ӧ����Ϣ
			// ���������б���Ϣ����,������
			MsgTeamList mt = new MsgTeamList();
			int len = 4 + 1 + 4 + 4 + 4;
			List<TeamInfo> teams = owerUser.getTeams();
			for (TeamInfo team : teams) {
				int uCount = team.getBudyList().size();
				len += 10 + 1;
				len += uCount * (10 + 4);
			}
			mt.setTotalLen(len);
			mt.setDest(owerUser.getykNum());
			mt.setSrc(IMsgConstance.Server_yk_NUMBER);
			mt.setType(IMsgConstance.command_teamList);
			mt.setTeamLists(owerUser.getTeams());
			this.sendMsg2Me(mt);// ���ͺ��ѷ����б�
			return true;
		}
		this.disConn();// ��½ʧ��,ֱ�ӶϿ�
		return false;
	}

	// �߳���ִ�н�����Ϣ�ķ���
	public void run() {
		try {
			/**
			 * ��ȡ��һ����Ϣ�����ʱ���֮Ϊ��ͬ����Ϣ���ȴ�����Ϣ���������Ϣ����������ܽ��к�������ִ�С�
			 * �����ֿ��ܣ�1  ��½  2 ע��
			 */
			loginOK = readFirstMsg(); //��ȡ��һ����Ϣ
			
			
			if (loginOK) {
				// �����½�ɹ�������������̶߳�����뵽������
				ChatTools.addClient(this.owerUser, this);
			}
			while (loginOK) {
				MsgHead msg = this.reciveData();// ѭ��������Ϣ
				ChatTools.sendMsg2One(this.owerUser, msg);// �ַ�������Ϣ
			}
		} catch (Exception ef) {
			LogTools.ERROR(this.getClass(), "����������Ϣ����:" + ef);
		}// �û�������,�Ӷ������Ƴ�����û���Ӧ�Ĵ����̶߳���
		ChatTools.removeClient(this.owerUser);
	}

	/**
	 * ���������϶�ȡ���ݿ�,���Ϊ��Ϣ����
	 * @return:�����������ݿ����Ϊ��Ϣ����
	 */
	private MsgHead reciveData() throws Exception {
		int len = dins.readInt(); // ��ȡ��Ϣ����
		LogTools.INFO(this.getClass(), "����������Ϣ����:" + len);
		byte[] data = new byte[len - 4];
		dins.readFully(data);
		/**
		 * ����data�������������� 
		 * ���������ǽ�data�ֽ������е����ݽ�������Ӧ����Ϣ���������
		 */
		MsgHead msg = ToolsParseMsg.parseMsg(data);// ����Ϊ��Ϣ����
		LogTools.INFO(this.getClass(), "������������Ϣ����:" + msg);
		return msg;
	}

	/**
	 * ����һ����Ϣ�������������������Ŀͻ����û�
	 * 
	 * @param msg
	 *            :Ҫ���͵���Ϣ����
	 * @return:�Ƿ��ͳɹ�
	 */
	public boolean sendMsg2Me(MsgHead msg) {
		try {
			byte[] data = ToolsCreateMsg.packMsg(msg);// ����Ϣ������Ϊ�ֽ���
			this.dous.write(data);
			this.dous.flush();
			LogTools.INFO(this.getClass(), "������������Ϣ����:" + msg);
			return true;
		} catch (Exception ef) {
			LogTools.ERROR(this.getClass(), "������������Ϣ����:" + msg);
		}
		return false;
	}

	/**
	 * �Ͽ�������������߳���ͻ���������, �����쳣,�����߳��˳�ʱ����
	 */
	public void disConn() {
		try {
			loginOK = false;
			this.client.close();
		} catch (Exception ef) {
		}
	}
}