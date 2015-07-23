package com.once.xenapi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;

import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class VMNetworkQoS extends XenAPIObject {

	protected final String ref;

	VMNetworkQoS(String ref) {
		this.ref = ref;
	}

	@Override
	public String toWireString() {
		return this.ref;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof VMNetworkQoS) {
			VMNetworkQoS other = (VMNetworkQoS) obj;
			return other.ref.equals(this.ref);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return ref.hashCode();
	}

	public static class Record implements Types.Record {
		public String toString() {
			StringWriter writer = new StringWriter();
			PrintWriter print = new PrintWriter(writer);
			print.printf("%1$20s: %2$s\n", "uuid", this.uuid);
			print.printf("%1$20s: %2$s\n", "iface", this.iface);
			print.printf("%1$20s: %2$s\n", "iface_rate", this.iface_rate);
			print.printf("%1$20s: %2$s\n", "iface_burst", this.iface_burst);
			return writer.toString();
		}

		public Map<String, Object> toMap() {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("uuid", this.uuid == null ? "" : this.uuid);
			map.put("iface", this.iface == null ? "" : this.iface);
			map.put("iface_rate", this.iface_rate == null ? 0 : this.iface_rate);
			map.put("iface_burst", this.iface_burst == null ? 0
					: this.iface_burst);
			return map;
		}

		/**
		 * Unique identifier/object reference
		 */
		public String uuid;
		/**
		 * Interface name
		 */
		public String iface;
		/**
		 * Current rate of interface
		 */
		public Long iface_rate;
		/**
		 * Current burst beyond rate
		 */
		public Long iface_burst;
	}

	public VMNetworkQoS.Record getRecord(Connection c)
			throws BadServerResponse, XenAPIException, XmlRpcException {
		String method_call = "VM_network_qos.get_record";
		String session = c.getSessionReference();
		Object[] method_params = { Marshalling.toXMLRPC(session),
				Marshalling.toXMLRPC(this.ref) };
		Map response = c.dispatch(method_call, method_params);
		Object result = response.get("Value");
		return Types.toVMNetworkQoSRecord(result);
	}

	public static VMNetworkQoS getByUuid(Connection c, String uuid)
			throws BadServerResponse, XenAPIException, XmlRpcException {
		String method_call = "VM_network_qos.get_by_uuid";
		String session = c.getSessionReference();
		Object[] method_params = { Marshalling.toXMLRPC(session),
				Marshalling.toXMLRPC(uuid) };
		Map response = c.dispatch(method_call, method_params);
		Object result = response.get("Value");
		return Types.toVMNetworkQoS(result);
	}

	public String getUuid(Connection c) throws BadServerResponse,
			XenAPIException, XmlRpcException {
		String method_call = "VM_network_qos.get_uuid";
		String session = c.getSessionReference();
		Object[] method_params = { Marshalling.toXMLRPC(session),
				Marshalling.toXMLRPC(this.ref) };
		Map response = c.dispatch(method_call, method_params);
		Object result = response.get("Value");
		return Types.toString(result);
	}

	public String getIface(Connection c) throws BadServerResponse,
			XenAPIException, XmlRpcException {
		String method_call = "VM_network_qos.get_iface";
		String session = c.getSessionReference();
		Object[] method_params = { Marshalling.toXMLRPC(session),
				Marshalling.toXMLRPC(this.ref) };
		Map response = c.dispatch(method_call, method_params);
		Object result = response.get("Value");
		return Types.toString(result);
	}

	public Long getIfaceRate(Connection c) throws BadServerResponse,
			XenAPIException, XmlRpcException {
		String method_call = "VM_network_qos.get_iface_rate";
		String session = c.getSessionReference();
		Object[] method_params = { Marshalling.toXMLRPC(session),
				Marshalling.toXMLRPC(this.ref) };
		Map response = c.dispatch(method_call, method_params);
		Object result = response.get("Value");
		return Types.toLong(result);
	}

	public Long getIfaceBurst(Connection c) throws BadServerResponse,
			XenAPIException, XmlRpcException {
		String method_call = "VM_network_qos.get_iface_burst";
		String session = c.getSessionReference();
		Object[] method_params = { Marshalling.toXMLRPC(session),
				Marshalling.toXMLRPC(this.ref) };
		Map response = c.dispatch(method_call, method_params);
		Object result = response.get("Value");
		return Types.toLong(result);
	}

	public void setIfaceRate(Connection c, long rate) throws BadServerResponse,
			XenAPIException, XmlRpcException {
		String method_call = "VM_network_qos.set_iface_rate";
		String session = c.getSessionReference();
		Object[] method_params = { Marshalling.toXMLRPC(session),
				Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(rate) };
		Map response = c.dispatch(method_call, method_params);
		Object result = response.get("Value");
		return;
	}

	public void setIfaceBurst(Connection c, long burst) throws BadServerResponse,
			XenAPIException, XmlRpcException {
		String method_call = "VM_network_qos.set_iface_burst";
		String session = c.getSessionReference();
		Object[] method_params = { Marshalling.toXMLRPC(session),
				Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(burst) };
		Map response = c.dispatch(method_call, method_params);
		Object result = response.get("Value");
		return;
	}
}
