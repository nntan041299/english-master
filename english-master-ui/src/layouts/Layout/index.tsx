import { ReactNode, useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import SideBar from "@/components/SideBar";
import Header from "@/components/Header";
import { getUserInfo } from "@/service/user";
import { setUserInfo } from "@/redux/user";
import { selectUser } from "@/redux/user/selectors";
import { AppDispatch } from "@/redux/store";

interface LayoutProps {
  children: ReactNode;
}

const Layout = ({ children }: LayoutProps) => {
  const dispatch = useDispatch<AppDispatch>();
  const { id } = useSelector(selectUser);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    if (!id) {
      getUserInfo().then((res) => {
        dispatch(setUserInfo(res.data.data));
      });
    }
  }, [dispatch, id]);

  return (
    <div className="flex h-screen overflow-hidden">
      <SideBar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex-1 flex flex-col overflow-hidden min-w-0">
        <Header onMenuToggle={() => setSidebarOpen((prev) => !prev)} />
        <main className="flex-1 overflow-y-auto bg-stone-50">{children}</main>
      </div>
    </div>
  );
};

export default Layout;
