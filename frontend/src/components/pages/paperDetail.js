import React from "react";
import { Layout} from "antd";
import Header from '../header';
import Footer from '../footer';
import { useLocation, useNavigate } from "react-router-dom";
import DetailCard from "../detailCard";

// 论文详情页面
function PaperDetail() {
    const location = useLocation();
    const navigate = useNavigate();
    // 获取传递过来的数据
    const paper = location.state?.paper;

    if (!paper) {
        // 如果没有数据，返回上一页或者展示一个错误提示
        return (
          <div>
            <p>没有找到文章数据。</p>
            <button onClick={() => navigate(-1)}>返回</button>
          </div>
        );
      }

    return (
        <Layout className='paperDetail'>
            <Layout.Header 
                style={{
                    padding: 0,
                    backgroundColor: 'white',
                    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                    zIndex: 10,
                    position: 'sticky',
                    top: 0
                }}
                >
                <Header/>
            </Layout.Header>
            <Layout.Content style={{minHeight: 700,}}>
                <div className='paperDetailContent'>
                    <DetailCard paperInfo={paper}/>
                </div>
            </Layout.Content>
            <Layout.Footer style={{
                backgroundColor:'#b82e28',
            }}>
                <Footer/>
            </Layout.Footer>
        </Layout>
    );
}

export default PaperDetail;