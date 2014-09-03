import java.lang.reflect.*;
import org.junit.*;
import org.junit.rules.*;
import java.util.*;
import static org.junit.Assert.*;

/* 
 * JUnit test class
 */
public class DUTInfoJUnit {

	//----------------------------- Test Case Definition --------------------------//
	@Test
		public void shouldNotHave_0_in_failedTestList() {
			shouldNotHave_0_in_( "failedTestList" );
		}

	@Test
		public void shouldNotHave_0_in_categorylist() {
			shouldNotHave_0_in_( "categoryList" );
		}

	@Test
		public void shouldNotHave_duplicates_in_failedTestList() {
			shouldNotHave_duplicates_in_( "failedTestList" );
		}

	@Test
		public void shouldNotHave_duplicates_in_categorylist() {
			shouldNotHave_duplicates_in_( "categoryList" );
		}


	//--------------------------------  Common Settings  --------------------------//

	private void shouldNotHave_0_in_( String fdName ) {
		for ( int dut : KTestSystem.getDut( KDutGroupType.SDUT ) ) {
			CBox dutInfo = CBox.build("javaapi.SS").yield( "dutInfo" ).yield( "DUTList", dut - 1 );
			if ( (Boolean)dutInfo.yield( fdName ).call( "contains", 0 ) ) {
				fail("DUT"+dut+" has 0 in "+fdName);
			} 
		}
	}

	private void shouldNotHave_duplicates_in_( String fdName ) {

		for ( int dut : KTestSystem.getDut( KDutGroupType.SDUT ) ) {
			CBox ftl = CBox.build("javaapi.SS").yield( "dutInfo" ).yield( "DUTList", dut - 1 ).yield( fdName );

			Set<Integer> fts = new HashSet<Integer>();
			for ( int i=0; i<(Integer)(ftl.call("size")); i++ ) {
				fts.add( (Integer)ftl.call("get", i));
			}

			if ( (Integer)ftl.call( "size" ) != fts.size() ) {
				fail("DUT"+dut+" has duplicated numbers in "+fdName);
			}
		}
	}



	@BeforeClass
		public static void setUp() {
		}

	@AfterClass
		public static void sumUp() {
		}

}

