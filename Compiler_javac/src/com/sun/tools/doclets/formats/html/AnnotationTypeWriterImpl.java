/*
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.tools.doclets.formats.html;

import com.sun.javadoc.*;
import com.sun.tools.doclets.internal.toolkit.*;
import com.sun.tools.doclets.internal.toolkit.util.*;
import com.sun.tools.doclets.internal.toolkit.builders.*;
import com.sun.tools.doclets.formats.html.markup.*;

/**
 * Generate the Class Information Page.
 * @see ClassDoc
 * @see java.util.Collections
 * @see java.util.List
 * @see java.util.ArrayList
 * @see java.util.HashMap
 *
 * @author Atul M Dambalkar
 * @author Robert Field
 * @author Bhavesh Patel (Modified)
 */
public class AnnotationTypeWriterImpl extends SubWriterHolderWriter
        implements AnnotationTypeWriter {

    protected AnnotationTypeDoc annotationType;

    protected Type prev;

    protected Type next;

    /**
     * @param annotationType the annotation type being documented.
     * @param prevType the previous class that was documented.
     * @param nextType the next class being documented.
     */
    public AnnotationTypeWriterImpl (AnnotationTypeDoc annotationType,
            Type prevType, Type nextType)
    throws Exception {
        super(ConfigurationImpl.getInstance(),
              DirectoryManager.getDirectoryPath(annotationType.containingPackage()),
              annotationType.name() + ".html",
              DirectoryManager.getRelativePath(annotationType.containingPackage().name()));
        this.annotationType = annotationType;
        configuration.currentcd = annotationType.asClassDoc();
        this.prev = prevType;
        this.next = nextType;
    }

    /**
     * Get this package link.
     *
     * @return a content tree for the package link
     */
    protected Content getNavLinkPackage() {
        Content linkContent = getHyperLink("package-summary.html", "",
                packageLabel);
        Content li = HtmlTree.LI(linkContent);
        return li;
    }

    /**
     * Get the class link.
     *
     * @return a content tree for the class link
     */
    protected Content getNavLinkClass() {
        Content li = HtmlTree.LI(HtmlStyle.navBarCell1Rev, classLabel);
        return li;
    }

    /**
     * Get the class use link.
     *
     * @return a content tree for the class use link
     */
    protected Content getNavLinkClassUse() {
        Content linkContent = getHyperLink("class-use/" + filename, "", useLabel);
        Content li = HtmlTree.LI(linkContent);
        return li;
    }

    /**
     * Get link to previous class.
     *
     * @return a content tree for the previous class link
     */
    public Content getNavLinkPrevious() {
        Content li;
        if (prev != null) {
            Content prevLink = new RawHtml(getLink(new LinkInfoImpl(
                    LinkInfoImpl.CONTEXT_CLASS, prev.asClassDoc(), "",
                    configuration.getText("doclet.Prev_Class"), true)));
            li = HtmlTree.LI(prevLink);
        }
        else
            li = HtmlTree.LI(prevclassLabel);
        return li;
    }

    /**
     * Get link to next class.
     *
     * @return a content tree for the next class link
     */
    public Content getNavLinkNext() {
        Content li;
        if (next != null) {
            Content nextLink = new RawHtml(getLink(new LinkInfoImpl(
                    LinkInfoImpl.CONTEXT_CLASS, next.asClassDoc(), "",
                    configuration.getText("doclet.Next_Class"), true)));
            li = HtmlTree.LI(nextLink);
        }
        else
            li = HtmlTree.LI(nextclassLabel);
        return li;
    }

    /**
     * {@inheritDoc}
     */
    public Content getHeader(String header) {
        String pkgname = (annotationType.containingPackage() != null)?
            annotationType.containingPackage().name(): "";
        String clname = annotationType.name();
        Content bodyTree = getBody(true, getWindowTitle(clname));
        addTop(bodyTree);
        addNavLinks(true, bodyTree);
        bodyTree.addContent(HtmlConstants.START_OF_CLASS_DATA);
        HtmlTree div = new HtmlTree(HtmlTag.DIV);
        div.addStyle(HtmlStyle.header);
        if (pkgname.length() > 0) {
            Content pkgNameContent = new StringContent(pkgname);
            Content pkgNamePara = HtmlTree.P(HtmlStyle.subTitle, pkgNameContent);
            div.addContent(pkgNamePara);
        }
        LinkInfoImpl linkInfo = new LinkInfoImpl(
                LinkInfoImpl.CONTEXT_CLASS_HEADER, annotationType, false);
        Content headerContent = new StringContent(header);
        Content heading = HtmlTree.HEADING(HtmlConstants.CLASS_PAGE_HEADING, true,
                HtmlStyle.title, headerContent);
        heading.addContent(new RawHtml(getTypeParameterLinks(linkInfo)));
        div.addContent(heading);
        bodyTree.addContent(div);
        return bodyTree;
    }

    /**
     * {@inheritDoc}
     */
    public Content getAnnotationContentHeader() {
        return getContentHeader();
    }

    /**
     * {@inheritDoc}
     */
    public void addFooter(Content contentTree) {
        contentTree.addContent(HtmlConstants.END_OF_CLASS_DATA);
        addNavLinks(false, contentTree);
        addBottom(contentTree);
    }

    /**
     * {@inheritDoc}
     */
    public void printDocument(Content contentTree) {
        printHtmlDocument(configuration.metakeywords.getMetaKeywords(annotationType),
                true, contentTree);
    }

    /**
     * {@inheritDoc}
     */
    public Content getAnnotationInfoTreeHeader() {
        return getMemberTreeHeader();
    }

    /**
     * {@inheritDoc}
     */
    public Content getAnnotationInfo(Content annotationInfoTree) {
        return getMemberTree(HtmlStyle.description, annotationInfoTree);
    }

    /**
     * {@inheritDoc}
     */
    public void addAnnotationTypeSignature(String modifiers, Content annotationInfoTree) {
        annotationInfoTree.addContent(new HtmlTree(HtmlTag.BR));
        Content pre = new HtmlTree(HtmlTag.PRE);
        addAnnotationInfo(annotationType, pre);
        pre.addContent(modifiers);
        LinkInfoImpl linkInfo = new LinkInfoImpl(
                LinkInfoImpl.CONTEXT_CLASS_SIGNATURE, annotationType, false);
        Content name = new RawHtml (annotationType.name() +
                getTypeParameterLinks(linkInfo));
        if (configuration().linksource) {
            addSrcLink(annotationType, name, pre);
        } else {
            pre.addContent(HtmlTree.STRONG(name));
        }
        annotationInfoTree.addContent(pre);
    }

    /**
     * {@inheritDoc}
     */
    public void addAnnotationTypeDescription(Content annotationInfoTree) {
        if(!configuration.nocomment) {
            if (annotationType.inlineTags().length > 0) {
                addInlineComment(annotationType, annotationInfoTree);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addAnnotationTypeTagInfo(Content annotationInfoTree) {
        if(!configuration.nocomment) {
            addTagsInfo(annotationType, annotationInfoTree);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addAnnotationTypeDeprecationInfo(Content annotationInfoTree) {
        Content hr = new HtmlTree(HtmlTag.HR);
        annotationInfoTree.addContent(hr);
        Tag[] deprs = annotationType.tags("deprecated");
        if (Util.isDeprecated(annotationType)) {
            Content strong = HtmlTree.STRONG(deprecatedPhrase);
            Content div = HtmlTree.DIV(HtmlStyle.block, strong);
            if (deprs.length > 0) {
                Tag[] commentTags = deprs[0].inlineTags();
                if (commentTags.length > 0) {
                    div.addContent(getSpace());
                    addInlineDeprecatedComment(annotationType, deprs[0], div);
                }
            }
            annotationInfoTree.addContent(div);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addAnnotationDetailsMarker(Content memberDetails) {
        memberDetails.addContent(HtmlConstants.START_OF_ANNOTATION_TYPE_DETAILS);
    }

    /**
     * {@inheritDoc}
     */
    protected Content getNavLinkTree() {
        Content treeLinkContent = getHyperLink("package-tree.html",
                "", treeLabel, "", "");
        Content li = HtmlTree.LI(treeLinkContent);
        return li;
    }

    /**
     * Add summary details to the navigation bar.
     *
     * @param subDiv the content tree to which the summary detail links will be added
     */
    protected void addSummaryDetailLinks(Content subDiv) {
        try {
            Content div = HtmlTree.DIV(getNavSummaryLinks());
            div.addContent(getNavDetailLinks());
            subDiv.addContent(div);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DocletAbortException();
        }
    }

    /**
     * Get summary links for navigation bar.
     *
     * @return the content tree for the navigation summary links
     */
    protected Content getNavSummaryLinks() throws Exception {
        Content li = HtmlTree.LI(summaryLabel);
        li.addContent(getSpace());
        Content ulNav = HtmlTree.UL(HtmlStyle.subNavList, li);
        MemberSummaryBuilder memberSummaryBuilder = (MemberSummaryBuilder)
                configuration.getBuilderFactory().getMemberSummaryBuilder(this);
        Content liNavReq = new HtmlTree(HtmlTag.LI);
        addNavSummaryLink(memberSummaryBuilder,
                "doclet.navAnnotationTypeRequiredMember",
                VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED, liNavReq);
        addNavGap(liNavReq);
        ulNav.addContent(liNavReq);
        Content liNavOpt = new HtmlTree(HtmlTag.LI);
        addNavSummaryLink(memberSummaryBuilder,
                "doclet.navAnnotationTypeOptionalMember",
                VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL, liNavOpt);
        ulNav.addContent(liNavOpt);
        return ulNav;
    }

    /**
     * Add the navigation summary link.
     *
     * @param builder builder for the member to be documented
     * @param label the label for the navigation
     * @param type type to be documented
     * @param liNav the content tree to which the navigation summary link will be added
     */
    protected void addNavSummaryLink(MemberSummaryBuilder builder,
            String label, int type, Content liNav) {
        AbstractMemberWriter writer = ((AbstractMemberWriter) builder.
                getMemberSummaryWriter(type));
        if (writer == null) {
            liNav.addContent(getResource(label));
        } else {
            liNav.addContent(writer.getNavSummaryLink(null,
                    ! builder.getVisibleMemberMap(type).noVisibleMembers()));
        }
    }

    /**
     * Get detail links for the navigation bar.
     *
     * @return the content tree for the detail links
     */
    protected Content getNavDetailLinks() throws Exception {
        Content li = HtmlTree.LI(detailLabel);
        li.addContent(getSpace());
        Content ulNav = HtmlTree.UL(HtmlStyle.subNavList, li);
        MemberSummaryBuilder memberSummaryBuilder = (MemberSummaryBuilder)
                configuration.getBuilderFactory().getMemberSummaryBuilder(this);
        AbstractMemberWriter writerOptional =
                ((AbstractMemberWriter) memberSummaryBuilder.
                getMemberSummaryWriter(VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL));
        AbstractMemberWriter writerRequired =
                ((AbstractMemberWriter) memberSummaryBuilder.
                getMemberSummaryWriter(VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED));
        if (writerOptional != null){
            Content liNavOpt = new HtmlTree(HtmlTag.LI);
            writerOptional.addNavDetailLink(annotationType.elements().length > 0, liNavOpt);
            ulNav.addContent(liNavOpt);
        } else if (writerRequired != null){
            Content liNavReq = new HtmlTree(HtmlTag.LI);
            writerRequired.addNavDetailLink(annotationType.elements().length > 0, liNavReq);
            ulNav.addContent(liNavReq);
        } else {
            Content liNav = HtmlTree.LI(getResource("doclet.navAnnotationTypeMember"));
            ulNav.addContent(liNav);
        }
        return ulNav;
    }

    /**
     * Add gap between navigation bar elements.
     *
     * @param liNav the content tree to which the gap will be added
     */
    protected void addNavGap(Content liNav) {
        liNav.addContent(getSpace());
        liNav.addContent("|");
        liNav.addContent(getSpace());
    }

    /**
     * {@inheritDoc}
     */
    public AnnotationTypeDoc getAnnotationTypeDoc() {
        return annotationType;
    }
}
