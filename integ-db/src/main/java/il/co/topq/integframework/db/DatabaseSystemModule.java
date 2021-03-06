package il.co.topq.integframework.db;

import il.co.topq.integframework.assertion.Assert;
import il.co.topq.integframework.assertion.NumberCompareAssertion;
import il.co.topq.integframework.assertion.NumberCompareAssertion.CompareMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseSystemModule {

	private final JdbcTemplate template;
	private List<ResultSetPrinter> resultSetPrinterList;

	public DatabaseSystemModule(final DataSource dataSource) {
		super();
		this.template = new JdbcTemplate(dataSource);
	}

	@PostConstruct
	public void initResultSetPrinters() {
		resultSetPrinterList = new ArrayList<ResultSetPrinter>();
		resultSetPrinterList.addAll(tablePrinters());
	}

	/**
	 * Override this method if you want to add more table printers. For example
	 * if you want to make use of the ResultSetHTMLPrinter
	 * 
	 * @return The table printers to add for each query
	 */
	protected List<ResultSetPrinter> tablePrinters() {
		List<ResultSetPrinter> printers = new ArrayList<ResultSetPrinter>();
		printers.add(new ResultSetHTMLPrinter());
		return printers;
	}

	/**
	 * 
	 * @param sql
	 * @return A list that representing the result set of the given query
	 */
	public List<Map<String, Object>> getResultList(final String sql) {
		if (null == sql || sql.isEmpty()) {
			throw new IllegalArgumentException("SQL query can't be empty");
		}

		List<Map<String, Object>> resultList = template.queryForList(sql);
		for (ResultSetPrinter printer : resultSetPrinterList) {
			printer.print(resultList);
		}
		return resultList;
	}

	/**
	 * Compare between the number of rows returned as a result of executing the
	 * specified query and the given number.
	 * 
	 * @param sql
	 *            SQL query
	 * @param expectedNumOfRows
	 *            the number of rows that are expected to return from the query
	 * @param compareMethod
	 *            The relations between the actual number of rows and the
	 *            expected
	 * @throws Exception
	 */
	public void assertNumOfRows(final String sql, int expectedNumOfRows, CompareMethod compareMethod) throws Exception {
		int actual = getResultList(sql).size();
		Assert.assertLogic(actual, new NumberCompareAssertion(expectedNumOfRows, compareMethod));
	}

	public void assertNumOfRows(final String sql, int expectedNumOfRows) throws Exception {
		assertNumOfRows(sql, expectedNumOfRows, CompareMethod.EQUALS);
	}

	/**
	 * @param table
	 * @return the number of rows in the specified table
	 */
	public int countRowsInTable(final String table) {
		if (null == table || table.isEmpty()) {
			throw new IllegalArgumentException("Table name can't be empty");
		}
		return getResultList(String.format("SELECT * FROM %s", table)).size();
	}

}
