
import java.lang.reflect.*;
import java.util.Arrays;


public class CBox {
	public Class<?> cls;
	public Type typ;
	public Object obj = null;

	private CBox( String clsName ) {
		try {
			this.cls = Class.forName( clsName );
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	private CBox( Object obj, Field fd ) {
		this.obj = obj; 
		this.cls = fd.getType();
		this.typ   = fd.getGenericType();
	}

	private CBox( Object obj ) {
		this.obj = obj;
		this.cls = obj.getClass();
	}

	private CBox( Object obj, Type typ ) {
		this.obj = obj;
		this.cls = obj.getClass();
		this.typ = typ;
	}

	private CBox( Class<?> cls ) {
		this.cls = cls;
	}

	public static CBox build ( Object obj, Field fd ) {
		return new CBox ( obj, fd );
	}

	public static CBox build ( Class<?> cls ) {
		return new CBox ( cls );
	}

	public static CBox build ( String clsName ) {
		return new CBox ( clsName );
	}

	public static CBox build ( Object obj ) {
		return new CBox ( obj );
	}

	public CBox makeInstance( Object...args ) {
		try {
			Constructor<?> ctor = this.cls.getDeclaredConstructor( getPrimitiveTypes( args ) );
			ctor.setAccessible( true );	// to access private member
			this.obj = ctor.newInstance( args );
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
		return this;
	}

	public Object get ( String fdName ) {
		try {
			return field( fdName ).get( this.obj );
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	public Object get ( String fdName, int idx ) {// retrieve element from an array field
		try {
			if (field( fdName ).getType().isArray()) {
				return Array.get( get( fdName ), idx );
			}
			throw new RuntimeException( "cannot apply this method on non-array fields" );
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	public CBox yieldClass ( String fdName ) { // construct CB instance for inner class
		try {
			return CBox.build( getInnerClass( fdName ) );
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	public CBox yield ( String fdName ) {// construct CB instance for the field
		try {
			return CBox.build( get(fdName), field( fdName ) );
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	public CBox yield ( String fdName, int idx ) {
		try {
			return CBox.build( get( fdName, idx ) );
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	public CBox set ( String fdName, Object v ) {
		try {
			field( fdName ).set ( this.obj, v );
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
		return this;
	}

	public Object call ( String mdName, Object...args ) {
		try {
			for ( Method md : this.cls.getMethods() ) {
				if ( mdName.equals ( md.getName() ) ) {
					try {
						md.setAccessible( true );
						return md.invoke ( this.obj, args );
					} catch ( IllegalArgumentException e ) {
						continue; // to try next signature
					}
				}
			}
		}catch ( Exception e ) {
			throw new RuntimeException ( e );
		}

		throw new RuntimeException ( new NoSuchMethodException( mdName + "(" + 
											Arrays.toString(getTypes ( args )) + ")" ) );
	}


	@Override
		public String toString() {
			return this.obj.toString();
		}

	public Field field ( String fdName ) throws NoSuchFieldException {
		Field fd = this.cls.getDeclaredField( fdName );
		fd.setAccessible(true); // to access private member
		return fd;
	}

	public Method method ( String mdName, Class<?>...types ) throws NoSuchMethodException {
		Method md = this.cls.getDeclaredMethod( mdName, types );
		md.setAccessible(true); // to access private member
		return md;
	}

	private static Class<?>[] getTypes ( Object...args ) {
		Class<?>[] typs = new Class<?>[ args.length ];
		for ( int i=0; i<args.length; i++ ) {
			typs[ i ] = args[ i ].getClass();
		}
		return typs;
	}

	private static Class<?>[] getPrimitiveTypes ( Object...args ) {
		Class<?>[] typs = new Class<?>[ args.length ];
		for ( int i=0; i<args.length; i++ ) {
			typs[ i ] = toPrimitiveType( args[ i ] );
		}
		return typs;
	}

	private static Class<?> toPrimitiveType ( Object o ) {
		Class<?> c = o.getClass();
		if ( c.equals ( Integer.class ) ) {
			c = int.class;
		} 
		else if ( c.equals ( Long.class ) ) {
			c = long.class;
		} 
		else if ( c.equals ( Double.class ) ) {
			c = double.class;
		} 
		else if ( c.equals ( Short.class ) ) {
			c = short.class;
		} 
		return c;
	}

	public Class<?> getInnerClass ( String innName ) {
		for ( Class<?> ic : this.cls.getDeclaredClasses() )  {
			if ( innName.equals ( ic.getSimpleName() ) ) {
				return ic;
			}
		}
		throw new RuntimeException( "cannot find inner class " + innName );
	}

	public static Object getEnumConstant ( String name ) {
		String errMsg = "should specify string like \"<package>.<Class>$<InnerClass>.<field>\"";
		String[] fullNames = name.split("\\$");
		if ( 2 != fullNames.length ) throw new RuntimeException( errMsg );
		String[] innName = fullNames[1].split("\\.");
		if ( 2 != innName.length ) throw new RuntimeException( errMsg );
		try {
			return CBox.build(fullNames[0]).yieldClass( innName[ 0 ] ).get ( innName[ 1 ] );
		} catch ( Exception e ) { 
			throw new RuntimeException( e );
		}
	}

}

