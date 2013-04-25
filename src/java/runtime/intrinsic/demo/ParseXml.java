/**
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2009-2013 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute
 * this file in accordance with the terms of the MIT license,
 * a copy of which can be found in the LICENSE.txt file or at
 * http://opensource.org/licenses/MIT.
 */
package runtime.intrinsic.demo;

import compile.type.Type;
import compile.type.Types;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import runtime.rep.Record;
import runtime.rep.Symbol;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;
import runtime.rep.map.MapValue;
import runtime.rep.map.PersistentMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;

/**
 * Tempoary hookup for demos--simple xml string to xnode
 *
 * @author Basil Hosmer
 */
public final class ParseXml extends IntrinsicLambda
{
    public static final String NAME = "parsexml";

    public static final Type TYPE = Types.fun(Types.STRING, XNode.INSTANCE.getType());

    public String getName()
    {
        return NAME;
    }

    public Type getType()
    {
        return TYPE;
    }

    public Object apply(final Object arg)
    {
        return invoke((String)arg);
    }

    public static Record invoke(final String s)
    {
        try
        {
            final StringReader reader = new StringReader(s);
            final InputSource source = new InputSource(reader);
            final Document doc = BUILDER.parse(source);
            reader.close();

            return nodeToXNode(doc.getDocumentElement());
        }
        catch (Exception e)
        {
            return makeXNode("$err",
                PersistentMap.EMPTY.assoc("exception", e.getClass().getName()).
                    assoc("message", e.getLocalizedMessage()),
                PersistentList.EMPTY);
        }
    }

    /**
     * xnode from DOM node
     */
    private static Record nodeToXNode(final Node node)
    {
        // name
        final String name = node.getLocalName();

        // attrs
        MapValue attrs = PersistentMap.EMPTY;
        final NamedNodeMap attrNodes = node.getAttributes();
        for (int i = 0; i < attrNodes.getLength(); i++)
        {
            final Node attrNode = attrNodes.item(i);
            attrs = attrs.assoc(
                Symbol.get(attrNode.getLocalName()), attrNode.getNodeValue());
        }

        // elems
        ListValue elems = PersistentList.EMPTY;

        final NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++)
        {
            final Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                final Element elemNode = (Element)childNode;
                elems = elems.append(nodeToXNode(elemNode));
            }
        }

        return makeXNode(name, attrs, elems);
    }

    /**
     * XNode builder
     * TODO currently the structure of values returned here needs to be
     * maintained in strict behind-the-scenes correspondence with
     * {@link compile.type.intrinsic.XNode}.
     * In particular, since we generate ordinal record access for monomorphic
     * values, fields must be declared in the same order. Once the XNode type
     * and the prototype for parsexml() can be declared in source, this fragile
     * piece of hackery can disappear.
     */

    private static final Symbol NAME_KEY = Symbol.get(XNode.XNODE_NAME);
    private static final Symbol ATTRS_KEY = Symbol.get(XNode.XNODE_ATTRS);
    private static final Symbol ELEMS_KEY = Symbol.get(XNode.XNODE_ELEMS);

    private static final Object[] KEYS = new Object[]{NAME_KEY, ATTRS_KEY, ELEMS_KEY};

    private static Record makeXNode(final String name, final MapValue attrs,
        final ListValue elems)
    {
        return Record.from(KEYS, new Object[]{name, attrs, elems});
    }

    /**
     * xml dom parser
     */
    private static final DocumentBuilderFactory FACTORY =
        DocumentBuilderFactory.newInstance();

    static
    {
        FACTORY.setNamespaceAware(true);
    }

    private static final DocumentBuilder BUILDER;

    static
    {
        DocumentBuilder builder = null;
        try
        {
            builder = FACTORY.newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        BUILDER = builder;
    }
}
