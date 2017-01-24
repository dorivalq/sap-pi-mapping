<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
<xsl:key name="firstname-search" match="firstname" use="$firstname"/>
		<html>
			<body>
				<h2>Students</h2>
				<table border="1">
					<tr bgcolor="#9acd32">
						<th>Roll No</th>
						<td>
							firstname::
						</td>
					</tr>
					<xsl:variable name="firstname-search11" select="$firstname" />
					<!-- key('firstname-search', 'Dinkar') does not working -->
					<xsl:for-each select="key('firstname-search', 'Dinkar')">
						<tr>
							<td>
								<xsl:value-of select="@rollno" />
							</td>
							<td>
								<xsl:value-of select="@firstname" />
							</td>
						</tr>
					</xsl:for-each>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>