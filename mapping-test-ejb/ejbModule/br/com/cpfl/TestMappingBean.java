package br.com.cpfl;

/**
 * Session Bean implementation class TestMappingBean
 */
@Stateless
public class TestMappingBean implements TestMapping, TestMappingBeanRemote, TestMappingBeanLocal {

	/**
	 * Default constructor.
	 */
	public TestMappingBean() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void executeQuery() {
		// TODO Auto-generated method stub

	}

}
