<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="app">
        <units>
            <xsl:apply-templates select="package/class"/>
        </units>
    </xsl:template>

    <xsl:template match="package/class">
        <unit>
            <name>
                <xsl:value-of select="@id"/>
            </name>
            <package>
                <xsl:value-of select="../@id"/>
            </package>
            <methods>
                <xsl:apply-templates select="methods/method"/>
            </methods>
        </unit>
    </xsl:template>

    <xsl:template match="methods/method">
        <method>
            <name>
                <xsl:value-of select="@name"/>
            </name>
            <operations>
                <xsl:apply-templates select="ops/op"/>
            </operations>
        </method>
    </xsl:template>

    <xsl:template match="ops/op">
        <operation>
            <type>
                <xsl:value-of select="@code"/>
            </type>
            <target>
                <xsl:value-of select="current()"/>
            </target>
        </operation>
    </xsl:template>

</xsl:stylesheet>
