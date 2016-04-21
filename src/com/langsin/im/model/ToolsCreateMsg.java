package com.langsin.im.model;

 import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import com.langsin.im.msg.IMsgConstance;
import com.langsin.im.msg.MsgAddFriend;
import com.langsin.im.msg.MsgAddFriendResp;
import com.langsin.im.msg.MsgChatFile;
import com.langsin.im.msg.MsgChatText;
import com.langsin.im.msg.MsgFindResp;
import com.langsin.im.msg.MsgHead;
import com.langsin.im.msg.MsgLogin;
import com.langsin.im.msg.MsgLoginResp;
import com.langsin.im.msg.MsgReg;
import com.langsin.im.msg.MsgRegResp;
import com.langsin.im.msg.MsgTeamList;
import com.langsin.im.utils.LogTools;


 
/**
 *��ʱͨ��ϵͳ:�����Ϣ������
 *����Ϣ���󣬸���Э���Լ�����Ϊ�ֽ������Ա����緢��
 */
public class ToolsCreateMsg {
	 
	/**
	 * ����Ϣ������Ϊ�ֽ����鷵��
	 * @param msg :Ҫ�������Ϣ����
	 * @return:���ص��ֽ�����
	 */
	public static byte[] packMsg(MsgHead msg)throws IOException{
		//�����ڴ������
		java.io.ByteArrayOutputStream bous=new java.io.ByteArrayOutputStream();
		java.io.DataOutputStream dous=new java.io.DataOutputStream(bous);
		 writeHead(dous,msg);//��д����Ϣͷ,������Ϣͷ�ṹ��ͬ
		int msgType=msg.getType();//ȡ����Ϣ���ͱ�ʶ
       if(msgType==IMsgConstance.command_login){//��½����
			 MsgLogin ml=(MsgLogin)msg;
			 writeString(dous,10,ml.getPwd());
		}
		else if(msgType==IMsgConstance.command_login_resp){//��½Ӧ��
			 MsgLoginResp mlr=(MsgLoginResp)msg;
			 dous.writeByte(mlr.getState());			
		}
		else if(msgType==IMsgConstance.command_reg){//ע������
			 MsgReg ml=(MsgReg)msg;
			 writeString(dous,10,ml.getNikeName());
			 writeString(dous,10,ml.getPwd());
		}
		else if(msgType==IMsgConstance.command_reg_resp){//ע��Ӧ��
			MsgRegResp mr=(MsgRegResp)msg;
			 dous.writeByte(mr.getState());
	      }
		else if(msgType==IMsgConstance.command_chatText){//��½Ӧ��
			 MsgChatText mt=(MsgChatText)msg;
			 dous.write(mt.getMsgContent().getBytes());
		}
		else if(msgType==IMsgConstance.command_chatFile){//������ļ����ݰ�
			 MsgChatFile mt=(MsgChatFile)msg;
			 writeString(dous,256,mt.getFileName());
			 dous.write(mt.getFileData());//д���ļ�����
		}
		else if(msgType==IMsgConstance.command_find_resp){
			MsgFindResp mf=(MsgFindResp)msg;
			List<UserInfo> users=mf.getUsers();
			dous.writeInt(users.size());
			for(UserInfo user:users){
				writeString(dous,10,user.getNikeName());
				dous.writeInt(user.getykNum());
			}
		}
		else if(msgType==IMsgConstance.command_teamList){//��������б����ݰ�
			MsgTeamList mdb= (MsgTeamList)msg;
			 List<TeamInfo> teams=mdb.getTeamLists();
			 int listsCount=teams.size();//�������
			 //д���ж��ٸ�����
			 dous.writeInt(listsCount);
			 for(TeamInfo team:teams){
				 //д��һ����������
				 writeString(dous,10,team.getName());
				   List<UserInfo> users=team.getBudyList();
				 //д�������ж��ٸ����Ѷ���
				 dous.writeByte(users.size());
				 for(UserInfo user:users){
					 //д��ÿһ�����ѵ�yk��
					 dous.writeInt(user.getykNum());
					 //д��ÿһ�����ѵ��س�
					 writeString(dous,10,user.getNikeName());
				 }
			 }
		} //���Ӻ���:      
		 else if(msgType==IMsgConstance.command_addFriend){
			 MsgAddFriend mt=(MsgAddFriend)msg;
			 //Ҫ����ĺ��ѵĺ���
			 dous.writeInt(mt.getFriendykNum());
	        }//���Ӻ��ѵ�Ӧ��:
		 else if(msgType==IMsgConstance.command_addFriend_Resp){
			 MsgAddFriendResp mt=(MsgAddFriendResp)msg;
			 dous.writeInt(mt.getFriendykNum());//Ҫ����ĺ��ѵĺ���
			 writeString(dous,10,mt.getFriendNikeName());
		}
		 else if(msgType==IMsgConstance.command_offLine
			||msgType==IMsgConstance.command_onLine
			||msgType==IMsgConstance.command_find){//����Ϣ��
			 
	 }else{
    String logMsg="����δ֪��Ϣ���ͣ��޷����:type:"+msgType;
	LogTools.ERROR(ToolsCreateMsg.class, logMsg);//��¼��־
		  }
         dous.flush();
		byte[] data=bous.toByteArray();
		return data;//���ش���������,�Է��㷢��
	}
	
	/**
	 * ͳһ������������д����Ϣͷ����
	 * @param dous:Ҫд���������
	 * @param m:��Ϣͷ����
	 */
	private static void writeHead(DataOutputStream dous,MsgHead m)
	throws IOException{
		dous.writeInt(m.getTotalLen());
		dous.writeByte(m.getType());
		dous.writeInt(m.getDest());
		dous.writeInt(m.getSrc());
	}
	 /**
	 * ������д��len���ȵ��ַ���
	 * ���s���ֽڳ��Ȳ���len������'\0'
	 * @param dous:���������
	 * @param len:Ҫд��ĳ���
	 * @param s:Ҫд�뵽���е��ַ���
	 */
	private static void writeString(DataOutputStream dous,int len,String s)
	throws IOException{
		byte[] data=s.getBytes();
		if(data.length>len){
			throw new IOException("д�볤��Ϊ"+data.length+",����!");
		}
		dous.write(data);
		 while(len>data.length){//����̣���Ҫ��0
			 dous.writeByte('\0');//��������0
			 len--;
		 }
	}
}