import React from "react";
import '../styles/detailCard.css';
import { Popover } from "antd";

function DetailCard({ paperInfo }) {
    // 论文详细数据展示页面
    console.log(paperInfo);
  return (
    <div className="detailBackground">
        <div className="detail-paperTitle">
            <h2 className="paperTitle">{paperInfo.article_title}</h2>
        </div>
        <div className="sectionForPaper">
            <div className="section-author">
                <h2 className="infoTitle" style={{color:'#b82e28'}}>作者信息</h2>
                <div className="authorInfo">
                    <div className="info-item">
                        <div className="authorLabel">作者：</div>
                        <p>{paperInfo.author_fullname}</p>
                    </div>
                    <div className="info-item">
                        <div className="authorLabel">通讯作者：</div>
                        <p>{paperInfo.author_fullname}</p>
                    </div>
                    <div className="info-item">
                        <div className="authorLabel">作者机构：</div>
                        <p>{paperInfo.author_fullname}</p>
                    </div>
                </div>
            </div>
            <div className="section-publisher">
                <h2 className="infoTitle" style={{color:'#b82e28'}}>出版信息</h2>
                <div className="publisherInfo">
                    <div className="info-item">
                        <div className="publisherLabel">来源出版物：</div>
                        <p>{paperInfo.publisher}</p>
                    </div>
                    <div className="info-item">
                        <div className="publisherLabel">ISSN:</div>
                        <p>{paperInfo.identifier_issn}</p>
                    </div>
                    <div className="info-item">
                        <div className="publisherLabel">EISSN:</div>
                        <p>{paperInfo.identifier_eissn}</p>
                    </div>
                    <div className="info-item">
                        <div className="publisherLabel">年卷期：</div>
                        <p>{paperInfo.pubyear} {paperInfo.vol}卷 {paperInfo.issue}期</p>
                    </div>
                    <div className="info-item">
                        <div className="publisherLabel">语种：</div>
                        <p>{paperInfo.languages}</p>
                    </div>
                </div>
            </div>
        </div>
        <div className="sectionForPaper">
            <div className="section-discipline">
                <h2 className="infoTitle" style={{color:'#b82e28'}}>学科方向</h2>
                <div className="disciplineInfo">
                    <div className="info-item">
                        <div className="disciplineLabel">关键词：</div>
                        <p>{paperInfo.keyword}</p>
                    </div>

                    <div className="info-item">
                        <div className="disciplineLabel">学科类别：</div>
                        <p>{paperInfo.subheadings}: {paperInfo.subject_extended}</p>
                    </div>
                </div>
            </div>
            <div className="section-impact">
                <h2 className="infoTitle" style={{color:'#b82e28'}}>影响力</h2>
                <div className="impactInfo">
                    <div className="info-item">
                        <div className="impactLabel">引用次数：</div>
                        <p>{paperInfo.citation_count}</p>
                    </div>
                    <div className="info-item">
                        <div className="impactLabel">影响因子：</div>
                        <p>{paperInfo.download_count}</p>
                    </div>
                </div>
            </div>
        </div>
        <div className="sectionForPaper">
            <div className="section-abstract">
                <h2 className="infoTitle" style={{color:'#b82e28'}}>摘要</h2>
                <div className="abstractInfo">
                <div className="info-item">
                    <div className="abstractLabel">摘要：</div>
                        <p>{paperInfo.abstract_text}</p>
                </div>
                </div>
            </div>
        </div>
        <div className="sectionForPaper">
            <div className="section-link">
                <h2 className="infoTitle" style={{color:'#b82e28'}}>链接</h2>
                <div className="linkInfo">
                    <div className="info-item">
                        <div className="linkLabel">DOI:</div>
                        <p>{paperInfo.identifier_doi}</p>
                    </div>
                    <div className="info-item">
                        <div className="linkLabel">URL:</div>
                        <a href={`https://doi.org/${paperInfo.identifier_doi}`} target="_blank" rel="noopener noreferrer">
                            https://doi.org/{paperInfo.identifier_doi}
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
  );
}

export default DetailCard;