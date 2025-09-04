import React from "react";
import '../styles/paperCard.css';

function PaperCard({ paperInfo, onTitleClick }) {
    // HTML解码并保留特定标签的函数
    const decodeHtmlWithTags = (html) => {
        if (!html) return html;

        // 创建一个临时div来解析HTML
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;

        // 保留常见的格式标签，移除其他标签
        const allowedTags = ['i', 'em', 'b', 'strong', 'sup', 'sub', 'u'];
        const processNode = (node) => {
            if (node.nodeType === Node.TEXT_NODE) {
                return node.textContent;
            } else if (node.nodeType === Node.ELEMENT_NODE) {
                const tagName = node.tagName.toLowerCase();
                if (allowedTags.includes(tagName)) {
                    const content = Array.from(node.childNodes).map(processNode).join('');
                    return `<${tagName}>${content}</${tagName}>`;
                } else {
                    // 对于不允许的标签，只返回其内容
                    return Array.from(node.childNodes).map(processNode).join('');
                }
            }
            return '';
        };

        return Array.from(tempDiv.childNodes).map(processNode).join('');
    };

    // 渲染带HTML标签的内容
    const renderHtmlContent = (content) => {
        if (!content) return content;
        const processedContent = decodeHtmlWithTags(content);
        return <span dangerouslySetInnerHTML={{ __html: processedContent }} />;
    };

    // 论文卡片组件（用于搜索结果展示）
    return (
        <div className="paper-card">
            <h2 onClick={onTitleClick} style={{ cursor: 'pointer'}}>
                {paperInfo.article_title ?
                    renderHtmlContent(paperInfo.article_title) :
                    "标题信息不可用"
                }
            </h2>
            <div className="paper-writer">
                <span className="label bold">作者：</span>
                {paperInfo.author_fullname ?
                    renderHtmlContent(paperInfo.author_fullname) :
                    "暂无作者信息"
                }
            </div>
            <div className="paper-origin">
                <span className="label bold">数据库：</span>
                {paperInfo.database ?
                    renderHtmlContent(paperInfo.database) :
                    "暂无来源信息"
                }
            </div>
            <div className="paper-year">
                <span className="label bold">时间：</span>
                {paperInfo.pubmonth} {paperInfo.pubyear}
            </div>
            <div className="paper-doi">
                <span className="label bold">DOI: </span>
                {paperInfo.identifier_doi || "暂无DOI"}
            </div>
        </div>
    );
}

export default PaperCard;