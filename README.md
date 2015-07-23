# OnceCenter是实验室内部IaaS客户端。
注意：建议使用一个新的Eclipse Workspace。因为要设置Eclipse编码格式为GBK，使用新的workspace可以避免影响其他项目。
然后导入Eclipse，打开OnceCenter64.product，找到“运行”。
================================================================================
  Licensed to the Institute of Software Chinese Academy of Sciences (ISCAS).
  The ISCAS licenses this file to You; you may not use this file except in 
  compliance with the License.
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
================================================================================

                                 vCenter 5.4.5
                                 Release Notes

=========
CONTENTS:
=========
  1.修复存储无法重命名BUG
  2.修复虚拟机磁盘扩容超出可用容量BUG
  3.资源池、主机、虚拟机添加/显示描述信息
  4.为VM添加额外磁盘
  5.添加VM性能监控开关
  6.添加64位版本
  7.增加“性能”显示的标签
  8.增加 ha开关
  9.提供上传iso取消功能
 10.虚拟网卡与物理网卡绑定功能
 11.完善事件类型，支持如下事件：
	事件名称
	资源池相关事件
	新建资源池
	物理机加入资源池
	物理机离开资源池
	重命名资源池
	物理机相关事件
	为物理机添加描述信息
	开关物理机的HA功能
	重命名物理机
	重命名存储
	虚拟机相关事件
	虚拟机转换成模板
	删除快照
	删除虚拟机
	部署虚拟机工具
	强制重启虚拟机
	强制关闭虚拟机
	迁移虚拟机
	重启虚拟机
	远程备份
	更换虚拟机的镜像文件
	管理虚拟机上挂载的磁盘
	编辑虚拟机的磁盘信息
	为虚拟机添加描述信息
	重命名虚拟机
	恢复虚拟机
	新建快照
	回滚快照
	关闭虚拟机
	开启虚拟机
	从光盘启动虚拟机
	中断虚拟机
	虚拟机备份
	导出到其他资源池
	模板相关事件
	将模板转换成虚拟机
	模板备份
	模板远程备份
	重命名模板
	为模板添加描述信息
	从模板快速生成虚拟机

