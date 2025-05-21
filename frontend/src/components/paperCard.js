import React from "react";
import '../styles/paperCard.css';

function PaperCard({ paperInfo, onTitleClick }) {
    // 论文卡片组件（用于搜索结果展示）
    return (
        <div className="paper-card">
            <h2 onClick={onTitleClick} style={{ cursor: 'pointer'}}>{paperInfo.article_title}</h2>
            <div className="paper-writer">
                <span className="label bold">作者：</span>
                {paperInfo.author_fullname}
            </div>
            <div className="paper-origin">
                <span className="label bold">来源：</span>
                {paperInfo.journal_title_source}
            </div>
            <div className="paper-year">
                <span className="label bold">时间：</span>
                {paperInfo.pubmonth} {paperInfo.pubyear}
            </div>
            <div className="paper-doi">
                <span className="label bold">DOI: </span>
                {paperInfo.identifier_doi}
            </div>
        </div>
    );
}

export default PaperCard;