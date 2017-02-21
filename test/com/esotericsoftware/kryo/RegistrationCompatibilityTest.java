package com.esotericsoftware.kryo;

import static com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegistrationCompatibilityTest extends KryoTestCase {

	public void testForwardRegistrationSerialization() throws Exception {
		// Do not register when serialize at first
		kryo.setDefaultSerializer(TaggedFieldSerializer.class);
		kryo.setRegistrationRequired(false);
		serializeDummy();

		// Deserialize without forcing registration
		DummyObject dummyObject = deserializeDummy();
		assertObject(dummyObject);

		// Reset kryo and force registration
		setUp();
		kryo.setRegistrationRequired(true);
		kryo.register(DummyObject.class);
		kryo.register(DummyObject.DummyInnerObject.class);
		kryo.register(DummyObject.DummyInnerObject.DummyInnerNestedObject.class);
		kryo.register(ArrayList.class);
		kryo.register(Date.class);
		dummyObject = deserializeDummy();
		assertObject(dummyObject);
	}

	public void testBackwardRegistrationSerialization() throws Exception {
		// Register when serialize at first
		kryo.setDefaultSerializer(TaggedFieldSerializer.class);
		kryo.setRegistrationRequired(true);
		kryo.register(DummyObject.class);
		kryo.register(DummyObject.DummyInnerObject.class);
		kryo.register(DummyObject.DummyInnerObject.DummyInnerNestedObject.class);
		kryo.register(ArrayList.class);
		kryo.register(Date.class);
		serializeDummy();

		// Deserialize while forcing registration
		DummyObject dummyObject = deserializeDummy();
		assertObject(dummyObject);

		// Reset kryo without forcing registration
		setUp();
		kryo.setRegistrationRequired(false);
		dummyObject = deserializeDummy();
		assertObject(dummyObject);
	}

	private void serializeDummy() throws FileNotFoundException {
		Output output = new Output(new FileOutputStream("compatibility.dat"));
		kryo.writeObject(output, new DummyObject());
		output.close();
	}

	private DummyObject deserializeDummy() throws FileNotFoundException {
		Input input = new Input(new FileInputStream("compatibility.dat"));
		DummyObject dataWrapper = kryo.readObject(input, DummyObject.class);
		input.close();
		return dataWrapper;
	}

	private void assertObject(DummyObject dummyObject) {
		assertEquals(dummyObject.someBoolean, false);
		assertEquals(dummyObject.someString, "abcde");
		assertEquals(dummyObject.someInt, 12345);

		assertEquals(dummyObject.someList.get(0), "first");
		assertEquals(dummyObject.someList.get(1), "second");
		assertEquals(dummyObject.someList.get(2), "third");

		assertEquals(dummyObject.someObject.innerBoolean, false);
		assertNotNull(dummyObject.someObject.innerDate);
		assertEquals(dummyObject.someObject.innerInt, 123);

		assertEquals(dummyObject.someObject.innerObject.innerInnerInt, 987);
		assertEquals(dummyObject.someObject.innerObject.innerInnerBoolean, false);
		assertNotNull(dummyObject.someObject.innerObject.innerInnerString, "innerThanInner");
	}

	//region Inner objects
	static public class DummyObject {
		@Tag(1)
		public String someString = "abcde";
		@Tag(2)
		public int someInt = 12345;
		@Tag(3)
		public boolean someBoolean = false;
		@Tag(4)
		public List<String> someList = new ArrayList();
		{
			someList.add("first");
			someList.add("second");
			someList.add("third");
		}
		@Tag(5)
		public Date someDate = new Date();
		@Tag(6)
		public DummyInnerObject someObject = new DummyInnerObject();

		static public class DummyInnerObject implements Serializable {
			private static final long serialVersionUID = -5694793454991873234L;

			@Tag(1)
			public String innerString = "test";
			@Tag(2)
			public boolean innerBoolean = false;
			@Tag(3)
			public int innerInt = 123;
			@Tag(4)
			public Date innerDate = new Date();
			@Tag(5)
			public DummyInnerNestedObject innerObject = new DummyInnerNestedObject();
			@Tag(6)
			public List<DummyInnerNestedObject> innerList = new ArrayList<DummyInnerNestedObject>();
			{
				innerList.add(new DummyInnerNestedObject());
				innerList.add(new DummyInnerNestedObject());
				innerList.add(new DummyInnerNestedObject());
			}

			public static class DummyInnerNestedObject {
				@Tag(1)
				public int innerInnerInt = 987;
				@Tag(2)
				public String innerInnerString = "innerThanInner";
				@Tag(3)
				public boolean innerInnerBoolean = false;
				@Tag(5)
				public Date innerInnerDate = new Date();
			}
		}
	}
	//endregion
}