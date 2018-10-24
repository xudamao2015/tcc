package com.tcc.core.serializer;

public interface ObjectSerializer<T> {

    /**
     * Serialize the given object to binary data.
     *
     * @param t object to serialize
     * @return the equivalent binary data
     */
    public byte[] serialize(T t);

    /**
     * Deserialize an object from the given binary data.
     *
     * @param bytes object binary representation
     * @return the equivalent object instance
     */
	@SuppressWarnings("hiding")
	public <T> T deserialize(byte[] bytes, Class<T> clazz);
}
