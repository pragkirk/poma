<?xml version="1.0"?>

<!--
  
  Takes the XML output from JarAnalyzer and transforms it
  into the 'dot' language used by Graphviz
  (http://www.research.att.com/sw/tools/graphviz/)
  to generate a project dependency graph.

  The jar files show up as rectangles with the jar name
  and the number of packages.  Arrows point to other jar files
  the jar depends on.  The rectangle is colored blue, but
  it turns to darker shades of red the further the jar file is
  from the 'main line'.

  Derived from JDepend xsl of the same function, contributed by
  David Bock.

-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text"/>
<xsl:template match="JarAnalyzer">
<Root-Element>
digraph g {
        graph [
	    rankdir = "LR"
	];
	node [
	    fontsize = "12"
	    fontname = "Courier"
	    shape = "ellipse"
	];
	edge[];
	<xsl:apply-templates select="Jars"/>
}
</Root-Element>
</xsl:template>

<xsl:template match="Jars">
    <xsl:apply-templates select="Jar" mode="node"/>
</xsl:template>

<xsl:template match="Jar" mode="node">
    <xsl:text>"</xsl:text><xsl:value-of select="@name"/> <xsl:text>" [
        label="</xsl:text><xsl:value-of
	select="@name"/><xsl:text> | Total Packages: </xsl:text><xsl:value-of select="Summary/Statistics/PackageCount/."/>
	<xsl:text>"
	shape="record"
	color=".99 </xsl:text>
        <xsl:choose>
            <xsl:when test="Summary/Metrics/Distance">
                <xsl:value-of select="Summary/Metrics/Distance/."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>0.0</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text> .9"
	style=filled
    ];
    </xsl:text>
    <xsl:apply-templates select="Summary/OutgoingDependencies"/>
</xsl:template>

<xsl:template match="Jar" mode="edge">
    <xsl:text>"</xsl:text><xsl:value-of select="../../../@name"/> <xsl:text>" -&gt; "</xsl:text><xsl:value-of select="."/><xsl:text>"
    </xsl:text>
</xsl:template>

<xsl:template match="Summary/OutgoingDependencies">
    <xsl:apply-templates select="Jar" mode="edge"/>
</xsl:template>

</xsl:stylesheet>
