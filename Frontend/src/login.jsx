import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldAlert, UserPlus, LogIn } from 'lucide-react';
import { toast } from 'react-toastify';
import api from './api';

export default function Login() {
    const [isLogin, setIsLogin] = useState(true);
    const [formData, setFormData] = useState({ name: '', email: '', password: '' });
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const endpoint = isLogin ? '/auth/login' : '/auth/register';
            const payload = isLogin
                ? { email: formData.email, password: formData.password }
                : formData;

            const res = await api.post(endpoint, payload);
            localStorage.setItem('jwt', res.data);
            toast.success(isLogin ? "Welcome back!" : "Registration successful!");

            try {
                const meRes = await api.get('/auth/me');
                const isAdmin = meRes.data?.role === 'ADMIN';
                navigate(isAdmin ? '/admin' : '/dashboard');
            } catch {
                navigate('/dashboard');
            }
        } catch (err) {
            toast.error(err.response?.data?.error || "Authentication failed. Check credentials.");
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-100 px-4">
            <div className="max-w-md w-full bg-white rounded-2xl shadow-xl p-8 border-t-4 border-blue-600">
                <div className="flex flex-col items-center mb-8">
                    <div className="bg-blue-100 p-3 rounded-full mb-3">
                        <ShieldAlert className="w-8 h-8 text-blue-600" />
                    </div>
                    <h2 className="text-2xl font-bold text-slate-800">SecureBank AML</h2>
                    <p className="text-sm text-slate-500">Enterprise Fraud Detection System</p>
                </div>

                <div className="flex bg-slate-100 p-1 rounded-lg mb-6">
                    <button onClick={() => setIsLogin(true)} className={`flex-1 py-2 text-sm font-medium rounded-md transition-all ${isLogin ? 'bg-white shadow-sm text-blue-600' : 'text-slate-500'}`}>Login</button>
                    <button onClick={() => setIsLogin(false)} className={`flex-1 py-2 text-sm font-medium rounded-md transition-all ${!isLogin ? 'bg-white shadow-sm text-blue-600' : 'text-slate-500'}`}>Register</button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    {!isLogin && (
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Full Name</label>
                            {/* Updated outline-none to outline-hidden for v4 */}
                            <input type="text" required className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-hidden" onChange={e => setFormData({...formData, name: e.target.value})} />
                        </div>
                    )}
                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Email Address</label>
                        <input type="email" required className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-hidden" onChange={e => setFormData({...formData, email: e.target.value})} />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Password</label>
                        <input type="password" required className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-hidden" onChange={e => setFormData({...formData, password: e.target.value})} />
                    </div>
                    <button type="submit" className="w-full bg-blue-600 text-white py-2.5 rounded-lg font-semibold hover:bg-blue-700 transition flex items-center justify-center gap-2 mt-6">
                        {isLogin ? <><LogIn className="w-5 h-5"/> Sign In</> : <><UserPlus className="w-5 h-5"/> Create Account</>}
                    </button>
                </form>
            </div>
        </div>
    );
}