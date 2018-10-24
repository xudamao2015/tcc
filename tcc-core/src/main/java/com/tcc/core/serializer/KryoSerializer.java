package com.tcc.core.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.tcc.core.domain.entity.TccActionInvocation;
import com.tcc.core.domain.entity.TccParticipant;
import com.tcc.core.domain.entity.TccTransaction;

/**
 * Kryo serialize
 * 
 * @author xuyi
 */
public class KryoSerializer<T> implements ObjectSerializer<T> {

	private static Kryo kryo = null;

	static {
		kryo = new Kryo();
		kryo.register(TccTransaction.class);
		kryo.register(TccParticipant.class);
		kryo.register(TccActionInvocation.class);
	}

	/**
	 * 序列化.
	 *
	 * @param 需要序更列化的对象
	 * @return 序列化后的byte 数组
	 */
	@Override
	public byte[] serialize(T obj) {
		Output output = new Output(256, -1);
		kryo.writeObject(output, obj);
		return output.toBytes();
	}

	/**
	 * 序列化.
	 *
	 * @param 需要序更列化的对象
	 * @return 序列化后的byte 数组
	 */
	@Override
	@SuppressWarnings("hiding")
	public <T> T deserialize(byte[] bytes, Class<T> clazz) {
		Input input = new Input(bytes);
		return (T) kryo.readObject(input, clazz);
	}
}
