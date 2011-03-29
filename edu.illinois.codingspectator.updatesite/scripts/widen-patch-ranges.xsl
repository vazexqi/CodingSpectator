<!-- Nick Chen wrote this script based on the script posted at
http://www.eclipse.org/newsportal/article.php?id=6375&group=eclipse.technology.equinox#6375
-->
<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/TR/xhtml1/strict" version="1.0">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	
	<xsl:template match="@range[../@name='org.eclipse.platform.feature.group']">
		<xsl:attribute name="range">[3.6.0,4.0.0)</xsl:attribute>
	</xsl:template>
	<xsl:template match="@range[../@name='org.eclipse.epp.usagedata.feature.feature.group']">
		<xsl:attribute name="range">[1.3.0,2.0.0)</xsl:attribute>
	</xsl:template>
	<xsl:template match="@range[../@name='org.eclipse.jdt.feature.group']">
		<xsl:attribute name="range">[3.6.0,4.0.0)</xsl:attribute>
	</xsl:template>
	
	<!-- Whenever you match any node or any attribute -->
	<xsl:template match="node()|@*">
		<!-- Copy the current node -->
		<xsl:copy>
			<!-- Including any attributes it has and any child nodes -->
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
