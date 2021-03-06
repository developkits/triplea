/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package games.strategy.engine.message;

import games.strategy.triplea.util.WrappedInvocationHandler;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Invocation handler for the UnifiedMessenger
 * 
 * 
 * @author Sean Bridges
 */
/**
 * Handles the invocation for a channel
 */
class UnifiedInvocationHandler extends WrappedInvocationHandler
{
	private final UnifiedMessenger m_messenger;
	private final String m_endPointName;
	private final boolean m_ignoreResults;
	private final Class<?> m_remoteType;
	
	public UnifiedInvocationHandler(final UnifiedMessenger messenger, final String endPointName, final boolean ignoreResults, final Class<?> remoteType)
	{
		// equality and hash code are bassed on end point name
		super(endPointName);
		m_messenger = messenger;
		m_endPointName = endPointName;
		m_ignoreResults = ignoreResults;
		m_remoteType = remoteType;
	}
	
	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
	{
		if (super.shouldHandle(method, args))
			return super.handle(method, args);
		if (args != null)
		{
			for (final Object o : args)
			{
				if (o != null && !(o instanceof Serializable))
				{
					throw new IllegalArgumentException(o + " is not serializable, all remote method args must be serializable.  method:" + method);
				}
			}
		}
		final RemoteMethodCall remoteMethodMsg = new RemoteMethodCall(m_endPointName, method.getName(), args, method.getParameterTypes(), m_remoteType);
		if (m_ignoreResults)
		{
			m_messenger.invoke(m_endPointName, remoteMethodMsg);
			return null;
		}
		else
		{
			final RemoteMethodCallResults response = m_messenger.invokeAndWait(m_endPointName, remoteMethodMsg);
			if (response.getException() != null)
			{
				if (response.getException() instanceof MessengerException)
				{
					final MessengerException cle = (MessengerException) response.getException();
					cle.fillInInvokerStackTrace();
				}
				else
				{
					// do not chain the exception, we want to keep whatever the original exception's class was, so just add our bit to the stack trace.
					final Throwable throwable = response.getException();
					final StackTraceElement[] exceptionTrace = throwable.getStackTrace();
					final Exception ourException = new Exception(throwable.getMessage() + " exception in response from other system");
					final StackTraceElement[] ourTrace = ourException.getStackTrace();// Thread.currentThread().getStackTrace();
					if (exceptionTrace != null && ourTrace != null)
					{
						final StackTraceElement[] combinedTrace = new StackTraceElement[(exceptionTrace.length + ourTrace.length)];
						int i = 0;
						for (int j = 0; j < exceptionTrace.length; j++)
						{
							combinedTrace[i] = exceptionTrace[j];
							i++;
						}
						for (int k = 0; k < ourTrace.length; k++)
						{
							combinedTrace[i] = ourTrace[k];
							i++;
						}
						throwable.setStackTrace(combinedTrace);
					}
				}
				throw response.getException();
			}
			return response.getRVal();
		}
	}
}
