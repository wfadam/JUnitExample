
import org.junit.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import static org.junit.Assert.*;

class MethodInfo {

	private Map<INFO, String> nameMap;
	private Map<INFO, Class[]> typeMap;
	private Map<INFO, Object[]> argsMap;

	MethodInfo(){
		nameMap = new EnumMap<INFO, String>(INFO.class);
		typeMap = new EnumMap<INFO, Class[]>(INFO.class);
		argsMap = new EnumMap<INFO, Object[]>(INFO.class);
	}

	public void add ( INFO item, Object obj ) {
		switch( item ) {
			case CLASS_NAME:
			case METHOD_NAME:
				nameMap.put ( item, (String)obj );
				break;
			case CONSTRUCTOR_TYPE:
			case METHOD_TYPE:
				typeMap.put ( item, (Class[])obj );
				break;
			case CONSTRUCTOR_ARGS:
			case METHOD_ARGS:
				argsMap.put ( item, (Object[])obj );
				break;
		}
	}

	public <T> T get ( INFO item, Class<T> cls ) {
		switch( item ) {
			case CLASS_NAME:
			case METHOD_NAME:
				return cls.cast( nameMap.get ( item ) );
			case CONSTRUCTOR_TYPE:
			case METHOD_TYPE:
				return cls.cast( typeMap.get ( item ) );
			case CONSTRUCTOR_ARGS:
			case METHOD_ARGS:
				return cls.cast( argsMap.get ( item ) );
			default:
				return null;
		}
	}

}

enum INFO {
	CLASS_NAME  , CONSTRUCTOR_TYPE , CONSTRUCTOR_ARGS , 
	METHOD_NAME , METHOD_TYPE      , METHOD_ARGS
}


class MethodBuilder {
	final MethodInfo mi;

	MethodBuilder( MethodInfo mi ){
		this.mi = mi;
	}

	public Object run() {
		return invoke( mi.get( INFO.METHOD_ARGS, Object[].class));
	}

	public Object invoke(Object...args) {
		Method md = this.methodOf (
				mi.get( INFO.CLASS_NAME, String.class),
				mi.get( INFO.METHOD_NAME, String.class),
				mi.get( INFO.METHOD_TYPE, Class[].class));
		Constructor constructor = this.constructorOf (
				mi.get( INFO.CLASS_NAME, String.class),
				mi.get( INFO.CONSTRUCTOR_TYPE, Class[].class));

		Object obj = null;
		Object rtn = null;
		try {
			obj = constructor.newInstance( 
					mi.get( INFO.CONSTRUCTOR_ARGS, Object[].class));
			rtn = md.invoke( obj, args );
		} catch ( Exception e ) {
			logIt( e );
		}
		return rtn;
	}

	private Method methodOf( String clsName, String mdName, Class...paramTypes ) {
		Method md = null;
		try {
			md = Class.forName( clsName ).getDeclaredMethod( mdName, paramTypes);
		} catch ( Exception e ) {
			logIt( e );
		}
		md.setAccessible(true); // for accessing private member
		return md;
	}


	private Constructor constructorOf(String clsName, Class...paramTypes) {
		Constructor constructor = null;
		try {
			constructor =  Class.forName( clsName ).getDeclaredConstructor( paramTypes );
			constructor.setAccessible(true);
		} catch ( Exception e ) {
			logIt( e );
		}
		return constructor;
	}


	private void logIt( Exception e ) {
		e.printStackTrace( );
	}
}



public class A {

	@BeforeClass
		public static void setUp() {

			MethodInfo mi = new MethodInfo();
			mi.add ( INFO.CLASS_NAME       , "javaapi.Action"								) ;
			mi.add ( INFO.CONSTRUCTOR_TYPE , new Class[]{ String.class  , Object[].class}	) ;
			mi.add ( INFO.CONSTRUCTOR_ARGS , new Object[]{ "NopPerChip" , new Object[]{}}	) ;
			mi.add ( INFO.METHOD_NAME      , "execute"										) ;
			mi.add ( INFO.METHOD_TYPE      , new Class[]{ int.class }   					) ;
			mi.add ( INFO.METHOD_ARGS      , new Object[]{ 10 }			   					) ;
			new MethodBuilder( mi ).run();
		}

	@Test
		public void dummyTest() {
			assertTrue( true );
		}


	@Test
		public void test_tb_940() {

			MethodInfo mi = new MethodInfo();
			mi.add ( INFO.CLASS_NAME  , "javaapi.Utility"                              ) ;
			mi.add ( INFO.METHOD_NAME , "get_category_from_fail_blk_count"             ) ;
			mi.add ( INFO.METHOD_TYPE , new Class[]{ int.class, int.class, int.class } ) ;

			int bbCnt = 9999;
			int testNum = (Integer)new MethodBuilder( mi ).invoke( 900, 40, bbCnt );;
			assertEquals( 940 ,testNum );

			bbCnt = 0;
			testNum = (Integer)new MethodBuilder( mi ).invoke( 900, 40, bbCnt );
			assertEquals( 900 ,testNum );
		} 


}
